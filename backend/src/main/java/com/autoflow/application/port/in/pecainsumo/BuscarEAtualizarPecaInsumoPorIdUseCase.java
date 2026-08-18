package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;

public interface BuscarEAtualizarPecaInsumoPorIdUseCase {
    PecaInsumoOutput execute(Long id);
}
