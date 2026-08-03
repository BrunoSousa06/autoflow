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
    public boolean executeSeExistir(Long orcamentoId) {
        return consultarPorOrcamentoUseCase.execute(orcamentoId)
                .map(reparo -> {
                    aprovarReparoAdicionalUseCase.execute(reparo.getId());
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void executeObrigatorio(Long orcamentoId) {
        var reparo = consultarPorOrcamentoUseCase.execute(orcamentoId)
                .orElseThrow(() -> new IllegalArgumentException("Reparo adicional não encontrado."));
        aprovarReparoAdicionalUseCase.execute(reparo.getId());
    }
}
