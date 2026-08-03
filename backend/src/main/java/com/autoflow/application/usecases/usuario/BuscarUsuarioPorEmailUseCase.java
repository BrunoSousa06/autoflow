package com.autoflow.application.usecases.usuario;

import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BuscarUsuarioPorEmailUseCase {

    private final UsuarioGateway usuarioGateway;

    public UsuarioEntity execute(String email) {

        return usuarioGateway.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário autenticado não encontrado."));
    }
}
