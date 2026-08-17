package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.port.in.cliente.BuscarClientePorEmailUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BuscarClientePorEmailUseCaseImpl implements BuscarClientePorEmailUseCase {

    private final ClienteGateway clienteGateway;

    @Override
    public ClienteOutput execute(String email) {
        return clienteGateway.findByUsuarioEmail(email)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
    }
}
