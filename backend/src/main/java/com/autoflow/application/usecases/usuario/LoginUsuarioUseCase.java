package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.LoginInput;
import com.autoflow.application.dto.usuario.LoginOutput;
import com.autoflow.application.gateway.AuthenticationGateway;
import com.autoflow.application.gateway.TokenGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class LoginUsuarioUseCase {

    private final AuthenticationGateway authenticationGateway;
    private final UsuarioGateway usuarioGateway;
    private final TokenGateway tokenGateway;

    public LoginOutput execute(LoginInput input) {

        authenticationGateway.authenticate(input.email(), input.senha());

        Usuario usuario = usuarioGateway
                .findByEmail(input.email())
                .orElseThrow();

        return new LoginOutput(tokenGateway.generateToken(
                usuario.getEmail(),
                usuario.getRole().name()
        ));
    }
}
