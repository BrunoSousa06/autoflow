package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListarTodosClientesUseCase {

    private final ClienteGateway clienteGateway;
    private final ClienteMapper clienteMapper;

    public List<ClienteOutput> execute() {
        List<ClienteEntity> todosClientes = clienteGateway.findAll();

        return clienteMapper.mapToListOutput(todosClientes);
    }
}
