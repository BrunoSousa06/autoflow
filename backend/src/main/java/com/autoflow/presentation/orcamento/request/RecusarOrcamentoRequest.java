package com.autoflow.presentation.orcamento.request;

import jakarta.validation.constraints.Size;

public record RecusarOrcamentoRequest(
        @Size(max = 500, message = "Motivo da recusa deve ter no máximo 500 caracteres")
        String motivo,
        @Size(max = 120, message = "Nome da assinatura deve ter no máximo 120 caracteres")
        String nome
) {
    public RecusarOrcamentoRequest(String motivo) {
        this(motivo, null);
    }
}
