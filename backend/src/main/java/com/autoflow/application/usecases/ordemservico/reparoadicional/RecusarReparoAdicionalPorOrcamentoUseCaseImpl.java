package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.port.in.ordemservico.reparoadicional.ConsultarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.port.in.ordemservico.reparoadicional.RecusarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.port.in.ordemservico.reparoadicional.RecusarReparoAdicionalUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class RecusarReparoAdicionalPorOrcamentoUseCaseImpl implements RecusarReparoAdicionalPorOrcamentoUseCase {

    private final ConsultarReparoAdicionalPorOrcamentoUseCase consultarPorOrcamentoUseCase;
    private final RecusarReparoAdicionalUseCase recusarReparoAdicionalUseCase;

    @TransactionalUseCase
    @Override
    public boolean executeSeExistir(Long orcamentoId, String motivo) {
        return consultarPorOrcamentoUseCase.execute(orcamentoId)
                .map(reparo -> {
                    recusarReparoAdicionalUseCase.execute(reparo.getId(), motivo);
                    return true;
                })
                .orElse(false);
    }
}
