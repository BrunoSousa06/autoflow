package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.service.orcamento.OrcamentoPublicacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrcamentoPublicacaoAdapter implements OrcamentoPublicacaoGateway {

    private final OrcamentoPublicacaoService publicacaoService;

    @Override
    public String publicar(Long orcamentoId) {
        return publicacaoService.publicar(orcamentoId).url();
    }
}
