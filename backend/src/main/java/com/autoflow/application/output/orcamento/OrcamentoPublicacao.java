package com.autoflow.application.output.orcamento;

/**
 * Links gerados para a publicação de um orçamento.
 */
public record OrcamentoPublicacao(
        String urlPdf,
        String urlDecisao
) {
}
