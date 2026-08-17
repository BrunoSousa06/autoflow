package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;

public interface CadastrarVeiculoUseCase {
    VeiculoOutput execute(CadastrarVeiculoInput input);
}
