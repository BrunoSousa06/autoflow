package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;

import java.util.List;

public interface ListarPecaInsumoUseCase {
    List<PecaInsumoOutput> execute();
}
