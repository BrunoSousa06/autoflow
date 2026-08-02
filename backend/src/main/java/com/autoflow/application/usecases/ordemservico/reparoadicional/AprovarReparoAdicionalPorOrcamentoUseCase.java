package com.autoflow.application.usecases.ordemservico.reparoadicional;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AprovarReparoAdicionalPorOrcamentoUseCase {

    private final ConsultarReparoAdicionalPorOrcamentoUseCase consultarPorOrcamentoUseCase;
    private final AprovarReparoAdicionalUseCase aprovarReparoAdicionalUseCase;

    @Transactional
    public void executeSeExistir(Long orcamentoId) {
        consultarPorOrcamentoUseCase.execute(orcamentoId)
                .ifPresent(reparo -> aprovarReparoAdicionalUseCase.execute(reparo.getId()));
    }

    @Transactional
    public void executeObrigatorio(Long orcamentoId) {
        var reparo = consultarPorOrcamentoUseCase.execute(orcamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));
        aprovarReparoAdicionalUseCase.execute(reparo.getId());
    }
}
