package com.autoflow.application.usecases.cliente;

import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.port.in.cliente.ListarTodosClientesUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ListarTodosClientesUseCaseImpl implements ListarTodosClientesUseCase {

    private final ClienteGateway clienteGateway;

    @Override
    public List<ClienteOutput> execute() {
        return clienteGateway.findAll();
    }
}
