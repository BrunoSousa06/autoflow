package com.autoflow.application.port.in.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;

import java.util.List;

public interface ListarTodosClientesUseCase {
    List<ClienteOutput> execute();
}
