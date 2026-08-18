package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.input.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.output.veiculo.VeiculoOutput;
import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.exception.VeiculoDuplicadoException;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.port.in.veiculo.CadastrarVeiculoUseCase;
import com.autoflow.application.policy.PlacaPolicy;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarVeiculoUseCaseImpl implements CadastrarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final VeiculoClienteGateway clienteGateway;

    @Override
    public VeiculoOutput execute(CadastrarVeiculoInput input) {
        Long clienteId = clienteGateway.findIdByCpfCnpj(input.cpfCnpj())
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado com o CPF/CNPJ: " + input.cpfCnpj()));

        String placa = PlacaPolicy.normalizar(input.placa());
        if (veiculoGateway.existsByPlaca(placa)) {
            throw new VeiculoDuplicadoException(
                    "Já existe um veículo cadastrado com a placa: " + input.placa());
        }

        CadastrarVeiculoInput inputNormalizado = new CadastrarVeiculoInput(
                input.cpfCnpj(),
                placa,
                input.marca(),
                input.modelo(),
                input.ano());

        return veiculoGateway.save(inputNormalizado, clienteId);
    }
}
