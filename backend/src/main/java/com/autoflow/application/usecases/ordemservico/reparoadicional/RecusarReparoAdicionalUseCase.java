package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RecusarReparoAdicionalUseCase {

    private final ReparoAdicionalGateway reparoAdicionalGateway;

    @TransactionalUseCase
    public ReparoAdicional execute(Long reparoAdicionalId, String motivo) {
        ReparoAdicional reparo = reparoAdicionalGateway.findByIdForUpdate(reparoAdicionalId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));

        reparo.recusar(motivo);
        return reparoAdicionalGateway.save(reparo);
    }
}
