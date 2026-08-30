package com.autoflow.presentation.orcamento.request;

import jakarta.validation.constraints.Size;

public record AprovarOrcamentoRequest(
        @Size(max = 120, message = "Nome da assinatura deve ter no máximo 120 caracteres")
        String nome
) {
}
