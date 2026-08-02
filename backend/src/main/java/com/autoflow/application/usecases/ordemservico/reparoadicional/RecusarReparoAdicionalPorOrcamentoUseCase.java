package com.autoflow.application.usecases.ordemservico.reparoadicional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecusarReparoAdicionalPorOrcamentoUseCase {

    private final ConsultarReparoAdicionalPorOrcamentoUseCase consultarPorOrcamentoUseCase;
    private final RecusarReparoAdicionalUseCase recusarReparoAdicionalUseCase;

    @Transactional
    public boolean executeSeExistir(Long orcamentoId, String motivo) {
        return consultarPorOrcamentoUseCase.execute(orcamentoId)
                .map(reparo -> {
                    recusarReparoAdicionalUseCase.execute(reparo.getId(), motivo);
                    return true;
                })
                .orElse(false);
    }
}
