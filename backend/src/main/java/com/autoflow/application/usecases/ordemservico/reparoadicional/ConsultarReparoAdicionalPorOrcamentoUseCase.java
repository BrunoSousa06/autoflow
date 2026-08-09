package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import lombok.RequiredArgsConstructor;

import java.util.Optional;


@RequiredArgsConstructor
public class ConsultarReparoAdicionalPorOrcamentoUseCase {

    private final ReparoAdicionalGateway reparoAdicionalGateway;

    public Optional<ReparoAdicionalEntity> execute(Long orcamentoId) {
        return reparoAdicionalGateway.findByOrcamentoId(orcamentoId);
    }
}
