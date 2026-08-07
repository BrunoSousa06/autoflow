package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.CadastrarVeiculoInput;
import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.policy.PlacaPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CadastrarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final VeiculoClienteGateway clienteGateway;

    public VeiculoOutput execute(CadastrarVeiculoInput input) {
        Long clienteId = clienteGateway.findIdByCpfCnpj(input.cpfCnpj())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente não encontrado com o CPF/CNPJ: " + input.cpfCnpj()));

        String placa = PlacaPolicy.normalizar(input.placa());
        if (veiculoGateway.existsByPlaca(placa)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
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
