package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.exception.VeiculoDadosInvalidosException;
import com.autoflow.application.exception.VeiculoDuplicadoException;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.input.veiculo.CadastrarVeiculoCommand;
import com.autoflow.application.input.veiculo.VeiculoInput;
import com.autoflow.application.output.veiculo.VeiculoOutput;
import com.autoflow.application.policy.PlacaPolicy;
import com.autoflow.application.port.in.veiculo.BuscarOuCadastrarVeiculoUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarOuCadastrarVeiculoUseCaseImpl implements BuscarOuCadastrarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    @Override
    public VeiculoOutput execute(Long clienteId, VeiculoInput input) {
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
                new CadastrarVeiculoCommand(null, placa, input.marca(), input.modelo(), input.ano()),
                clienteId);
    }
}
