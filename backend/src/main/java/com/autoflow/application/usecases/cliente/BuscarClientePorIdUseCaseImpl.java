package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.port.in.cliente.BuscarClientePorIdUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BuscarClientePorIdUseCaseImpl implements BuscarClientePorIdUseCase {

    private final ClienteGateway clienteGateway;

    @Override
    public ClienteOutput execute(Long id) {
        return clienteGateway.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado com o ID: " + id));
    }
}
