package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.input.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.output.veiculo.VeiculoOutput;

public interface CadastrarVeiculoUseCase {
    VeiculoOutput execute(CadastrarVeiculoInput input);
}
