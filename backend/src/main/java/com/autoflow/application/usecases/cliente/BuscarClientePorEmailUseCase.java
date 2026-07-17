package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


@Component
@RequiredArgsConstructor
public class BuscarClientePorEmailUseCase {

    private final ClienteGateway clienteGateway;
    private final ClienteMapper clienteMapper;

    public ClienteOutput execute(String email) {
        ClienteEntity clienteEmail = clienteGateway.findByUsuarioEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        return clienteMapper.mapToOutput(clienteEmail);
    }
}
