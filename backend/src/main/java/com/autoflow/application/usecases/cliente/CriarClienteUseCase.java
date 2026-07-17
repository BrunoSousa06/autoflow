package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteInput;
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
public class CriarClienteUseCase {

    private final ClienteGateway clienteGateway;
    private final ClienteMapper clienteMapper;

    public ClienteOutput execute(ClienteInput input) {
        if (clienteGateway.existsByCpfCnpj(input.cpfCnpj())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "CPF/CNPJ já cadastrado");
        }

        ClienteEntity save = clienteGateway.save(clienteMapper.mapToEntity(input));

        return  clienteMapper.mapToOutput(save);
    }
}
