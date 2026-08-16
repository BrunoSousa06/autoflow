package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarClienteUseCase {

    private final ClienteGateway clienteGateway;

    @TransactionalUseCase
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
