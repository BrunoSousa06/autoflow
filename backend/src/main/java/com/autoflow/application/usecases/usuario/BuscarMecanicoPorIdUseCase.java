package com.autoflow.application.usecases.usuario;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarMecanicoPorIdUseCase {

    private final UsuarioGateway usuarioGateway;

    public UsuarioEntity execute(Long mecanicoId) {

        UsuarioEntity usuario = usuarioGateway.findById(mecanicoId)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Mecânico não encontrado."));

        if (!RoleEnum.MECANICO.equals(usuario.getRole())) {
            throw ApplicationException.badRequest(
                    "Usuário informado não é um mecânico."
            );
        }

        return usuario;
    }
}
