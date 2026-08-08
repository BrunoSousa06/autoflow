package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.mapper.UsuarioApplicationMapper;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class BuscarMecanicosUseCase {

    private final UsuarioGateway usuarioGateway;
    private final UsuarioApplicationMapper usuarioMapper;

    public List<UsuarioOutput> execute() {

        return usuarioMapper.toOutput(
                usuarioGateway.findByRole(RoleEnum.MECANICO)
        );
    }
}
