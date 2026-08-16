package com.autoflow.application.usecases.usuario;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarUsuarioPorEmailUseCase {

    private final UsuarioGateway usuarioGateway;

    public Usuario execute(String email) {

        return usuarioGateway.findByEmail(email)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Usuário autenticado não encontrado."));
    }
}
