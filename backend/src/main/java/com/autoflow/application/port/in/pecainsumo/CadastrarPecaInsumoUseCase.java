package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;

public interface CadastrarPecaInsumoUseCase {
    PecaInsumoOutput execute(PecaInsumoInput request);
}
