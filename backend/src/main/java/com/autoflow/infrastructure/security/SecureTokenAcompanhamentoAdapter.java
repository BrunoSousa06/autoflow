package com.autoflow.infrastructure.security;

import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.application.output.ordemservico.acompanhamento.TokenAcompanhamentoOutput;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class SecureTokenAcompanhamentoAdapter implements TokenAcompanhamentoGateway {

    private static final int TOKEN_SIZE_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public TokenAcompanhamentoOutput gerar() {
        byte[] bytes = new byte[TOKEN_SIZE_BYTES];
        SECURE_RANDOM.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        return new TokenAcompanhamentoOutput(
                token,
                calcularHash(token)
        );
    }

    @Override
    public String calcularHash(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Token de acompanhamento é obrigatório"
            );
        }

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Algoritmo SHA-256 indisponível",
                    exception
            );
        }
    }
}