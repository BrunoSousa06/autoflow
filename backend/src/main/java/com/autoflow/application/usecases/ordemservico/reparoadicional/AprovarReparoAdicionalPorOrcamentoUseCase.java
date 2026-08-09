package com.autoflow.application.usecases.ordemservico.reparoadicional;

import com.autoflow.application.transaction.TransactionalUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AprovarReparoAdicionalPorOrcamentoUseCase {

    private final ConsultarReparoAdicionalPorOrcamentoUseCase consultarPorOrcamentoUseCase;
    private final AprovarReparoAdicionalUseCase aprovarReparoAdicionalUseCase;

    @TransactionalUseCase
    public boolean executeSeExistir(Long orcamentoId) {
        return consultarPorOrcamentoUseCase.execute(orcamentoId)
                .map(reparo -> {
                    aprovarReparoAdicionalUseCase.execute(reparo.getId());
                    return true;
                })
                .orElse(false);
    }

    @TransactionalUseCase
    public void executeObrigatorio(Long orcamentoId) {
        var reparo = consultarPorOrcamentoUseCase.execute(orcamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));
        aprovarReparoAdicionalUseCase.execute(reparo.getId());
    }
}
