package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.gateway.ReparoAdicionalGateway;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecusarReparoAdicionalUseCase {

    private final ReparoAdicionalGateway reparoAdicionalGateway;

    @Transactional
    public ReparoAdicionalEntity execute(Long reparoAdicionalId, String motivo) {
        ReparoAdicionalEntity reparo = reparoAdicionalGateway.findById(reparoAdicionalId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));

        reparo.recusar(motivo);
        return reparoAdicionalGateway.save(reparo);
    }
}
