package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.VeiculoOrdemServicoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.exception.VeiculoDadosInvalidosException;
import com.autoflow.application.exception.VeiculoDuplicadoException;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.policy.PlacaPolicy;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarOuCadastrarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public VeiculoOutput execute(Long clienteId, VeiculoOrdemServicoInput input) {
        String placa = PlacaPolicy.normalizar(input.placa());
        VeiculoOutput existente = veiculoGateway.findByPlaca(placa).orElse(null);

        if (existente != null) {
            if (!existente.clienteId().equals(clienteId)) {
                throw new VeiculoDuplicadoException("Placa já cadastrada para outro cliente.");
            }
            return existente;
        }

        if (input.marca() == null || input.marca().isBlank()
                || input.modelo() == null || input.modelo().isBlank()
                || input.ano() == null) {
            throw new VeiculoDadosInvalidosException(
                    "Marca, modelo e ano são obrigatórios para cadastrar um novo veículo.");
        }

        return veiculoGateway.save(
                new VeiculoOrdemServicoInput(placa, input.marca(), input.modelo(), input.ano()),
                clienteId);
    }
}
