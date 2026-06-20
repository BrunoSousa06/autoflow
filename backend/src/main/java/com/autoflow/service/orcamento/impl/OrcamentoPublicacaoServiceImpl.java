package com.autoflow.service.orcamento.impl;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import com.autoflow.service.orcamento.dto.PublicacaoOrcamentoResult;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class OrcamentoPublicacaoServiceImpl implements OrcamentoPublicacaoService {
    
    private final OrcamentoRepository orcamentoRepository;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;
    @Value("${app.public-token-secret:CHANGE_ME}")
    private String tokenSecret;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public PublicacaoOrcamentoResult publicar(Long orcamentoId){
        OrcamentoEntity orcamento = orcamentoRepository.findById(orcamentoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado."));
        
        if(orcamento.getStatus() != StatusOrcamento.DISPONIVEL){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Orçamento não esta disponivel para publicar.");
        }
        
        String token = gerarTokenUrl();
        String hash = sha256Hex(token + ":" + tokenSecret);
        
        orcamento.setPublicTokenHash(hash);
        
        if(orcamento.getDisponibilizadoEm() == null) orcamento.setDisponibilizadoEm(LocalDateTime.now());
        
        orcamentoRepository.save(orcamento);

        String urlPdf = publicBaseUrl + "/public/orcamentos/" + orcamento.getId() + "/pdf?token=" + token;

        return new PublicacaoOrcamentoResult(orcamento.getId(), urlPdf);
    }



    private String sha256Hex(String input){
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

    @Override
    public boolean validarToken(OrcamentoEntity orc, String token) {
        if (orc.getPublicTokenHash() == null) return false;
        String hash = sha256Hex(token + ":" + tokenSecret);
        return hash.equals(orc.getPublicTokenHash());
    }
    
    
}
