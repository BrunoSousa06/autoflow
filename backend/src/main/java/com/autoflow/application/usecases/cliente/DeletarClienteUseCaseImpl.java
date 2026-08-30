package com.autoflow.application.usecases.cliente;

import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.port.in.cliente.DeletarClienteUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeletarClienteUseCaseImpl implements DeletarClienteUseCase {

    private final ClienteGateway clienteGateway;

    @Override
    public void execute(Long id) {
        if (!clienteGateway.existsById(id)) {
            throw new ClienteNaoEncontradoException("Cliente não encontrado com o ID: " + id);
        }
        clienteGateway.deleteById(id);
    }
}
