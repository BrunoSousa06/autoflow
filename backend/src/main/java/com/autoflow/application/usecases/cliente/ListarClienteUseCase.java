package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.infrastructure.persistence.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


@Component
@RequiredArgsConstructor
public class ListarClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final ClienteMapper clienteMapper;

    public ClienteOutput execute(Long documento) {
        String identificador = String.valueOf(documento).replaceAll("\\D", "");

        if (identificador.matches("\\d{11}|\\d{14}")) {
            ClienteEntity clienteEntity = clienteGateway.findByCpfCnpj(identificador)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado com o CPF/CNPJ: " + identificador));
            return clienteMapper.mapToOutput(clienteEntity);
        }

        ClienteEntity clienteEntity = clienteGateway.findById(documento)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado com o ID: " + documento));

        return clienteMapper.mapToOutput(clienteEntity);
    }
}
