package com.autoflow.application.dto.notificacao;

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
        String urlPublica
) {
}
