package com.autoflow.service.orcamento;

public interface OrcamentoVersioningService {

    int proximaVersaoPrincipal(Long ordemServicId);

    void substituirDisponivelAtual(Long ordemServicoId);

    int proximaVersaoAdicional(Long ordemServicoId);
}
