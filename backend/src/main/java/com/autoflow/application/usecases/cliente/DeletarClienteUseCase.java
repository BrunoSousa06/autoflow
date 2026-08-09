package com.autoflow.application.usecases.cliente;

import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletarClienteUseCase {

    private final ClienteGateway clienteGateway;

    public void execute(Long id) {
        if (!clienteGateway.existsById(id)) {
            throw new ClienteNaoEncontradoException("Cliente não encontrado com o ID: " + id);
        }
        clienteGateway.deleteById(id);
    }
}
