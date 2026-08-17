package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;

public interface BuscarOuCadastrarVeiculoForOrdemServicoUseCase {
    VeiculoOutput execute(ClienteOutput cliente, VeiculoOrdemServicoInput input);
}
