package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.input.veiculo.VeiculoInput;
import com.autoflow.application.output.veiculo.VeiculoOutput;

public interface BuscarOuCadastrarVeiculoUseCase {
    VeiculoOutput execute(Long clienteId, VeiculoInput input);
}
