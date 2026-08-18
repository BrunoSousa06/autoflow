package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.application.port.in.ordemservico.reparoadicional.RecusarReparoAdicionalUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Clock;
import java.time.LocalDateTime;


public class RecusarReparoAdicionalUseCaseImpl implements RecusarReparoAdicionalUseCase {

    private final ReparoAdicionalGateway reparoAdicionalGateway;
    private final Clock clock;

    @Autowired
    public RecusarReparoAdicionalUseCaseImpl(ReparoAdicionalGateway reparoAdicionalGateway, Clock clock) {
        this.reparoAdicionalGateway = reparoAdicionalGateway;
        this.clock = clock;
    }

    public RecusarReparoAdicionalUseCaseImpl(ReparoAdicionalGateway reparoAdicionalGateway) {
        this(reparoAdicionalGateway, Clock.systemUTC());
    }

    @TransactionalUseCase
    @Override
    public ReparoAdicional execute(Long reparoAdicionalId, String motivo) {
        ReparoAdicional reparo = reparoAdicionalGateway.findByIdForUpdate(reparoAdicionalId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));

        reparo.recusar(motivo, LocalDateTime.now(clock));
        return reparoAdicionalGateway.save(reparo);
    }
}
