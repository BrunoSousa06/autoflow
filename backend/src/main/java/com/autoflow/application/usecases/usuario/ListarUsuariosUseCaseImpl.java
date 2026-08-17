package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.mapper.UsuarioApplicationMapper;
import com.autoflow.application.port.in.usuario.ListarUsuariosUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ListarUsuariosUseCaseImpl implements ListarUsuariosUseCase {

    private final UsuarioGateway usuarioGateway;
    private final UsuarioApplicationMapper usuarioMapper;

    @Override
    public List<UsuarioOutput> execute() {

        return usuarioMapper.toOutput(
                usuarioGateway.findAll()
        );
    }
}
