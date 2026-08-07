package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.domain.usuario.UsuarioEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CadastrarClienteUseCase {

    private final ClienteGateway clienteGateway;
    @Transactional
    public ClienteOutput execute(
            RegistroInput request,
            UsuarioEntity usuario) {

        if (clienteGateway.existsByCpfCnpj(request.cpfCnpj())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "CPF/CNPJ já cadastrado");
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
