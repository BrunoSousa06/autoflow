package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.dto.veiculo.VeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;

public interface AtualizarVeiculoUseCase {
    VeiculoOutput execute(Long id, VeiculoInput input);
}
