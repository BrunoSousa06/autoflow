package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.input.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.output.veiculo.VeiculoOutput;

public interface BuscarOuCadastrarVeiculoUseCase {
    VeiculoOutput execute(Long clienteId, VeiculoOrdemServicoInput input);
}
