package com.autoflow.service.orcamento;

public interface OrcamentoVersioningService {

    int proximaVersaoPrincipal(Long ordemServicId);

    int proximaVersaoPrincipalNumeroOs(String numeroOs);
}
