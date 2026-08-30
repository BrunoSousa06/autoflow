package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.application.port.in.ordemservico.reparoadicional.ConsultarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;


@RequiredArgsConstructor
public class ConsultarReparoAdicionalPorOrcamentoUseCaseImpl implements ConsultarReparoAdicionalPorOrcamentoUseCase {

    private final ReparoAdicionalGateway reparoAdicionalGateway;

    @Override
    public Optional<ReparoAdicional> execute(Long orcamentoId) {
        return reparoAdicionalGateway.findByOrcamentoId(orcamentoId);
    }
}
