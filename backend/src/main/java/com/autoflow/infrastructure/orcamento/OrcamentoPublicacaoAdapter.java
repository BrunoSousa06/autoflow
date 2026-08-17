package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.dto.orcamento.OrcamentoPublicacao;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class OrcamentoPublicacaoAdapter implements OrcamentoPublicacaoGateway {

    private final Clock clock;
    @Value("${app.public-token-secret:CHANGE_ME}")
    private String tokenSecret;
    @Value("${app.public-base-url:http://localhost:8081}")
    private String publicBaseUrl;
    @Value("${app.public-token-expiration-days:7}")
    private long tokenExpirationDays = 7;
    private final SecureRandom secureRandom = new SecureRandom();

    private final OrcamentoGateway orcamentoGateway;
    @Value("${app.frontend-public-base-url:http://localhost:4200}")
    private String frontendPublicBaseUrl;

    @Override
    @Transactional
    public String publicar(Long orcamentoId) {
        return publicarComLinks(orcamentoId).urlPdf();
    }

    @Override
    @Transactional
    public OrcamentoPublicacao publicarComLinks(Long orcamentoId) {
        OrcamentoEntity orcamento = orcamentoGateway.findById(orcamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado."));

        if (orcamento.getStatus() != StatusOrcamento.DISPONIVEL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento não esta disponivel para publicar.");
        }

        String token = gerarTokenUrl();
        String hash = sha256Hex(token + ":" + tokenSecret);

        orcamento.setPublicTokenHash(hash);

        LocalDateTime agora = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        orcamento.setPublicTokenExpiraEm(agora.plusDays(tokenExpirationDays));
        if (orcamento.getDisponibilizadoEm() == null) orcamento.setDisponibilizadoEm(agora);

        orcamentoGateway.save(orcamento);

        String urlPdf = publicBaseUrl + "/public/orcamentos/" + orcamento.getId() + "/pdf?token=" + token;
        String urlDecisao = frontendPublicBaseUrl + "/public/orcamentos/" + orcamento.getId() + "?token=" + token;

        return new OrcamentoPublicacao(urlPdf, urlDecisao);
    }

    @Override
    public boolean validarToken(OrcamentoEntity orcamento, String token) {
        if (token == null || orcamento.getPublicTokenHash() == null
                || orcamento.getPublicTokenExpiraEm() == null) return false;

        LocalDateTime agora = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
        if (!orcamento.getPublicTokenExpiraEm().isAfter(agora)) return false;

        String hash = sha256Hex(token + ":" + tokenSecret);
        return MessageDigest.isEqual(
                hash.getBytes(StandardCharsets.UTF_8),
                orcamento.getPublicTokenHash().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new InternalException(e.getMessage());
        }
    }

    private String gerarTokenUrl() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
