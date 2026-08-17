package com.autoflow.application.usecases.cliente;

import com.autoflow.application.input.cliente.ClienteInput;
import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.application.exception.ClienteDuplicadoException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.port.in.cliente.CriarClienteUseCase;
import com.autoflow.domain.cliente.Cliente;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CriarClienteUseCaseImpl implements CriarClienteUseCase {

    private final ClienteGateway clienteGateway;

    @Override
    public ClienteOutput execute(ClienteInput input) {
        Cliente.criar(input.nome(), input.cpfCnpj(), input.telefone(), input.email());
        if (clienteGateway.existsByCpfCnpj(input.cpfCnpj())) {
            throw new ClienteDuplicadoException("CPF/CNPJ já cadastrado");
        }

        return clienteGateway.save(input);
    }
}
