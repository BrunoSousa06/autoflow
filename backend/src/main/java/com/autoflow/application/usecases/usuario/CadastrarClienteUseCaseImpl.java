package com.autoflow.application.usecases.usuario;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.input.cliente.ClienteInput;
import com.autoflow.application.input.usuario.RegistroInput;
import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.application.port.in.usuario.CadastrarClienteUseCase;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarClienteUseCaseImpl implements CadastrarClienteUseCase {

    private final ClienteGateway clienteGateway;

    @TransactionalUseCase
    @Override
    public ClienteOutput execute(
            RegistroInput request,
            Usuario usuario) {

        if (clienteGateway.existsByCpfCnpj(request.cpfCnpj())) {
            throw ApplicationException.conflict("CPF/CNPJ já cadastrado");
        }

        ClienteInput input = new ClienteInput(
                request.nome(),
                request.cpfCnpj(),
                request.telefone(),
                request.email(),
                usuario.getId());

        return clienteGateway.save(input);
    }
}
