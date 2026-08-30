package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.application.input.pecainsumo.PecaInsumoInput;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;

public interface AtualizarPecaInsumoUseCase {
    PecaInsumoOutput execute(Long id, PecaInsumoInput request);
}
