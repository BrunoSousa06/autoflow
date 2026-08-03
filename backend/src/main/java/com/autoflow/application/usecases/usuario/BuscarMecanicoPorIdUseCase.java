package com.autoflow.application.usecases.usuario;

import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BuscarMecanicoPorIdUseCase {

    private final UsuarioGateway usuarioGateway;

    public UsuarioEntity execute(Long mecanicoId) {

        UsuarioEntity usuario = usuarioGateway.findById(mecanicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Mecânico não encontrado."));

        if (!RoleEnum.MECANICO.equals(usuario.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usuário informado não é um mecânico."
            );
        }

        return usuario;
    }
}
