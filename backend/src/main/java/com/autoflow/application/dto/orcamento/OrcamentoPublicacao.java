package com.autoflow.application.dto.orcamento;

/**
 * Links gerados para a publicação de um orçamento.
 */
public record OrcamentoPublicacao(
        String urlPdf,
        String urlDecisao
) {
}
