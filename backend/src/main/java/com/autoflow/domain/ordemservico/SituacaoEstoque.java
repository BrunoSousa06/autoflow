package com.autoflow.domain.ordemservico;

public record SituacaoEstoque(
        Integer quantidadeDisponivel,
        MotivoPendenciaItem motivoPendencia
) {
}