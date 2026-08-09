package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BuscarClientePorCpfCnpjUseCase {

    private final ClienteGateway clienteGateway;

    public ClienteOutput execute(String cpfCnpj) {
        return clienteGateway.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado com o CPF/CNPJ: " + cpfCnpj));
    }
}
