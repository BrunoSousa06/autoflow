package com.autoflow.application.usecases.cliente;

import com.autoflow.application.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class DeletarClienteUseCase {

    private final ClienteGateway clienteGateway;

    public void execute(Long id) {
        if (!clienteGateway.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado com o ID: " + id);
        }
        clienteGateway.deleteById(id);
    }
}
