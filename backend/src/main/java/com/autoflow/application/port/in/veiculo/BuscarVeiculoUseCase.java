package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.output.veiculo.VeiculoOutput;

public interface BuscarVeiculoUseCase {
    VeiculoOutput execute(Long id);
}
