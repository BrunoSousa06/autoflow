package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;

public interface BuscarPecaInsumoPorIdUseCase {
    PecaInsumoOutput execute(Long id);
}
