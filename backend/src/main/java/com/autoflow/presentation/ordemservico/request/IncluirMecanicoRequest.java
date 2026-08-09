package com.autoflow.presentation.ordemservico.request;

public record IncluirMecanicoRequest(
        Long mecanicoId,
        String mecanicoEmail
) {
}
