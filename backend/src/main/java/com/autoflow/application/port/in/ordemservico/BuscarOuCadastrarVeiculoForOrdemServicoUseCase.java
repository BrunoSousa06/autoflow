package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.application.input.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.output.veiculo.VeiculoOutput;

public interface BuscarOuCadastrarVeiculoForOrdemServicoUseCase {
    VeiculoOutput execute(ClienteOutput cliente, VeiculoOrdemServicoInput input);
}
