package com.autoflow.application.usecases.usuario;

import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.mapper.UsuarioApplicationMapper;
import com.autoflow.application.output.usuario.UsuarioOutput;
import com.autoflow.application.port.in.usuario.BuscarMecanicosUseCase;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class BuscarMecanicosUseCaseImpl implements BuscarMecanicosUseCase {

    private final UsuarioGateway usuarioGateway;
    private final UsuarioApplicationMapper usuarioMapper;

    @Override
    public List<UsuarioOutput> execute() {

        return usuarioMapper.toOutput(
                usuarioGateway.findByRole(RoleEnum.MECANICO)
        );
    }
}
