package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ListarTodosClientesUseCase {

    private final ClienteGateway clienteGateway;

    public List<ClienteOutput> execute() {
        return clienteGateway.findAll();
    }
}
