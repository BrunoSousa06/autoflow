package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.gateway.PasswordGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CadastrarUsuarioUseCase {

    private final UsuarioGateway usuarioGateway;
    private final PasswordGateway passwordGateway;
    private final CadastrarClienteUseCase cadastrarClienteUseCase;

    @Transactional
    public UsuarioOutput execute(RegistroInput request) {

        if (usuarioGateway.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email já cadastrado");
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
