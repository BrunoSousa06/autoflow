package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;

public interface BuscarEAtualizarPecaInsumoPorIdUseCase {
    PecaInsumoOutput execute(Long id);
}
