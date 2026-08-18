package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.application.port.in.ordemservico.reparoadicional.AprovarReparoAdicionalPorOrcamentoUseCase;
import com.autoflow.application.port.in.ordemservico.reparoadicional.AprovarReparoAdicionalUseCase;
import com.autoflow.application.port.in.ordemservico.reparoadicional.ConsultarReparoAdicionalPorOrcamentoUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AprovarReparoAdicionalPorOrcamentoUseCaseImpl implements AprovarReparoAdicionalPorOrcamentoUseCase {

    private final ConsultarReparoAdicionalPorOrcamentoUseCase consultarPorOrcamentoUseCase;
    private final AprovarReparoAdicionalUseCase aprovarReparoAdicionalUseCase;

    @TransactionalUseCase
    @Override
    public boolean executeSeExistir(Long orcamentoId) {
        return consultarPorOrcamentoUseCase.execute(orcamentoId)
                .map(reparo -> {
                    aprovarReparoAdicionalUseCase.execute(reparo.getId());
                    return true;
                })
                .orElse(false);
    }

    @TransactionalUseCase
    @Override
    public void executeObrigatorio(Long orcamentoId) {
        var reparo = consultarPorOrcamentoUseCase.execute(orcamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));
        aprovarReparoAdicionalUseCase.execute(reparo.getId());
    }
}
