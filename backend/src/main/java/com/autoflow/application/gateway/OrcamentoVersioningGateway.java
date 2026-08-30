package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.TipoOrcamento;

public interface OrcamentoVersioningGateway {

    int proximaVersao(Long ordemServicoId, TipoOrcamento tipo);

    int proximaVersaoPorNumeroOs(String numeroOs, TipoOrcamento tipo);

    void substituirDisponivelAtual(Long ordemServicoId, TipoOrcamento tipo);
}
