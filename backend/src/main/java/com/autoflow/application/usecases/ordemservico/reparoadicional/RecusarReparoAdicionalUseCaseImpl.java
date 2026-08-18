package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.application.port.in.ordemservico.reparoadicional.RecusarReparoAdicionalUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RecusarReparoAdicionalUseCaseImpl implements RecusarReparoAdicionalUseCase {

    private final ReparoAdicionalGateway reparoAdicionalGateway;

    @TransactionalUseCase
    @Override
    public ReparoAdicional execute(Long reparoAdicionalId, String motivo) {
        ReparoAdicional reparo = reparoAdicionalGateway.findByIdForUpdate(reparoAdicionalId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));

        reparo.recusar(motivo);
        return reparoAdicionalGateway.save(reparo);
    }
}
