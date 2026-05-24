package com.autoflow.ordemServico.domain;

import java.util.UUID;

public record ServicoSolicitado(UUID servicoId, String nome) {

    public ServicoSolicitado {
        if (servicoId == null) {
            throw new IllegalArgumentException("Servico e obrigatorio.");
        }

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do servico e obrigatorio.");
        }
    }
}
