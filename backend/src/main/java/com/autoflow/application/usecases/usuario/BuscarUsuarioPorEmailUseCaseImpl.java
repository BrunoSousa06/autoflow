package com.autoflow.application.usecases.usuario;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.port.in.usuario.BuscarUsuarioPorEmailUseCase;
import com.autoflow.domain.usuario.Usuario;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarUsuarioPorEmailUseCaseImpl implements BuscarUsuarioPorEmailUseCase {

    private final UsuarioGateway usuarioGateway;

    @Override
    public Usuario execute(String email) {

        return usuarioGateway.findByEmail(email)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Usuário autenticado não encontrado."));
    }
}
