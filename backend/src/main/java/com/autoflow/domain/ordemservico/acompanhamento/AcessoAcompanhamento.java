package com.autoflow.domain.ordemservico.acompanhamento;

import java.time.LocalDateTime;
import java.util.Objects;

public record AcessoAcompanhamento(
        String tokenHash,
        LocalDateTime criadoEm,
        LocalDateTime expiraEm,
        LocalDateTime revogadoEm
) {

    public AcessoAcompanhamento {
        Objects.requireNonNull(
                tokenHash,
                "Hash do token é obrigatório"
        );

        Objects.requireNonNull(
                criadoEm,
                "Data de criação é obrigatória"
        );

        if (tokenHash.isBlank()) {
            throw new IllegalArgumentException(
                    "Hash do token não pode estar vazio"
            );
        }

        if (expiraEm != null && !expiraEm.isAfter(criadoEm)) {
            throw new IllegalArgumentException(
                    "Expiração deve ser posterior à criação"
            );
        }
    }

    public boolean estaDisponivelEm(LocalDateTime dataHora) {
        Objects.requireNonNull(
                dataHora,
                "Data da validação é obrigatória"
        );

        if (revogadoEm != null) {
            return false;
        }

        return expiraEm == null || expiraEm.isAfter(dataHora);
    }
}