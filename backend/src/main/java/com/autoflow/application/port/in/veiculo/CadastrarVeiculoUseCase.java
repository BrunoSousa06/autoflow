package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.input.veiculo.CadastrarVeiculoCommand;
import com.autoflow.application.output.veiculo.VeiculoOutput;

public interface CadastrarVeiculoUseCase {
    VeiculoOutput execute(CadastrarVeiculoCommand input);
}
