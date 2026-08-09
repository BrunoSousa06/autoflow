package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PasswordGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.transaction.TransactionalUseCase;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarUsuarioUseCase {

    private final UsuarioGateway usuarioGateway;
    private final PasswordGateway passwordGateway;
    private final CadastrarClienteUseCase cadastrarClienteUseCase;

    @TransactionalUseCase
    public UsuarioOutput execute(RegistroInput request) {

        if (usuarioGateway.existsByEmail(request.email())) {
            throw ApplicationException.conflict("Email já cadastrado");
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenha(passwordGateway.encode(request.senha()));
        usuario.setRole(request.role());
        usuario = usuarioGateway.save(usuario);

        if (RoleEnum.CLIENTE.equals(request.role())) {
            cadastrarClienteUseCase.execute(request, usuario);
        }

        return new UsuarioOutput(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole()
        );
    }
}
