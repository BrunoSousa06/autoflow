package com.autoflow.application.port.in.veiculo;

import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;

public interface BuscarOuCadastrarVeiculoUseCase {
    VeiculoOutput execute(Long clienteId, VeiculoOrdemServicoInput input);
}
