package com.autoflow.application.usecases.cliente;

import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.application.port.in.cliente.BuscarClientePorCpfCnpjUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BuscarClientePorCpfCnpjUseCaseImpl implements BuscarClientePorCpfCnpjUseCase {

    private final ClienteGateway clienteGateway;

    @Override
    public ClienteOutput execute(String cpfCnpj) {
        return clienteGateway.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado com o CPF/CNPJ: " + cpfCnpj));
    }
}
