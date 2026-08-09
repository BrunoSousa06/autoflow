package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.transaction.TransactionalUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RecusarReparoAdicionalPorOrcamentoUseCase {

    private final ConsultarReparoAdicionalPorOrcamentoUseCase consultarPorOrcamentoUseCase;
    private final RecusarReparoAdicionalUseCase recusarReparoAdicionalUseCase;

    @TransactionalUseCase
    public boolean executeSeExistir(Long orcamentoId, String motivo) {
        return consultarPorOrcamentoUseCase.execute(orcamentoId)
                .map(reparo -> {
                    recusarReparoAdicionalUseCase.execute(reparo.getId(), motivo);
                    return true;
                })
                .orElse(false);
    }
}
