package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.service.orcamento.OrcamentoVersioningService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrcamentoVersioningAdapter implements OrcamentoVersioningGateway {
    private final OrcamentoVersioningService versioningService;

    @Override
    public int proximaVersaoPrincipalNumeroOs(String numeroOs) {
        return versioningService.proximaVersaoPrincipalNumeroOs(numeroOs);
    }
}
