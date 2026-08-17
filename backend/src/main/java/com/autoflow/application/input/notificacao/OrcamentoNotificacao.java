package com.autoflow.application.input.notificacao;

import com.autoflow.domain.orcamento.TipoOrcamento;

/**
 * Dados mínimos necessários para compor uma notificação de orçamento.
 */
public record OrcamentoNotificacao(
        Long orcamentoId,
        TipoOrcamento tipo,
        String numeroOs,
        String clienteNome,
        String clienteEmail,
        String urlPublica,
        String urlDecisao
) {
    public OrcamentoNotificacao(
            Long orcamentoId,
            TipoOrcamento tipo,
            String numeroOs,
            String clienteNome,
            String clienteEmail,
            String urlPublica
    ) {
        this(orcamentoId, tipo, numeroOs, clienteNome, clienteEmail, urlPublica, null);
    }
}
