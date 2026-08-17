package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.exception.ClienteDuplicadoException;
import com.autoflow.application.exception.ClienteNaoEncontradoException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.port.in.cliente.AtualizarClienteUseCase;
import com.autoflow.domain.cliente.Cliente;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AtualizarClienteUseCaseImpl implements AtualizarClienteUseCase {

    private final ClienteGateway clienteGateway;

    @Override
    public ClienteOutput execute(Long id, ClienteInput input) {
        Cliente.reconstituir(id, input.nome(), input.cpfCnpj(), input.telefone(), input.email());
        if (clienteGateway.findById(id).isEmpty()) {
            throw new ClienteNaoEncontradoException("Cliente não encontrado com o ID: " + id);
        }

        if (clienteGateway.existsByCpfCnpjAndIdNot(input.cpfCnpj(), id)) {
            throw new ClienteDuplicadoException("CPF/CNPJ já cadastrado");
        }

        return clienteGateway.update(id, input);
    }
}
