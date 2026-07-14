package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


@Component
@RequiredArgsConstructor
public class AtualizarClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final ClienteMapper clienteMapper;

    public ClienteOutput execute(Long id, ClienteInput input) {
        ClienteEntity clienteEntity = clienteGateway.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado com o ID: " + id));

        clienteMapper.updateEntity(input, clienteEntity);

        ClienteEntity updatedCliente = clienteGateway.save(clienteEntity);

        return clienteMapper.mapToOutput(updatedCliente);
    }
}
