package com.autoflow.application.usecases.cliente;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.application.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class BuscarClientePorCpfCnpjUseCase {

    private final ClienteGateway clienteGateway;

    public ClienteEntity execute(String cpfCnpj) {
        return clienteGateway.findByCpfCnpj(cpfCnpj)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado com o CPF/CNPJ: " + cpfCnpj));
    }
}
