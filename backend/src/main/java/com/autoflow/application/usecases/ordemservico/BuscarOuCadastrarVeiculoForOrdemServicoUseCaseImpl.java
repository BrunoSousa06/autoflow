package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.policy.PlacaPolicy;
import com.autoflow.application.port.in.ordemservico.BuscarOuCadastrarVeiculoForOrdemServicoUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BuscarOuCadastrarVeiculoForOrdemServicoUseCaseImpl implements BuscarOuCadastrarVeiculoForOrdemServicoUseCase {

    private final VeiculoGateway veiculoGateway;

    @Override
    public VeiculoOutput execute(ClienteOutput cliente, VeiculoOrdemServicoInput input) {
        String placa = PlacaPolicy.normalizar(input.placa());
        var existente = veiculoGateway.findByPlaca(placa);

        if (existente.isPresent()) {
            VeiculoOutput veiculo = existente.get();
            if (!veiculo.clienteId().equals(cliente.id())) {
                throw ApplicationException.conflict(
                        "Placa já cadastrada para outro cliente.");
            }
            return veiculo;
        }

        if (input.marca() == null || input.marca().isBlank()
                || input.modelo() == null || input.modelo().isBlank()
                || input.ano() == null) {
            throw ApplicationException.badRequest(
                    "Marca, modelo e ano são obrigatórios para cadastrar um novo veículo.");
        }

        return veiculoGateway.save(new VeiculoOrdemServicoInput(
                placa, input.marca(), input.modelo(), input.ano()), cliente.id());
    }
}
