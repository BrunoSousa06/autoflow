package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BuscarClientePorIdUseCase {

    private final ClienteGateway clienteGateway;

    public ClienteOutput execute(Long id) {
        return clienteGateway.findById(id)
                .orElseThrow(() -> new ClienteNaoEncontradoException(
                        "Cliente não encontrado com o ID: " + id));
    }
}
