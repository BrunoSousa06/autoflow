package com.autoflow.application.usecases.usuario;

import com.autoflow.application.dto.usuario.UsuarioOutput;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.infrastructure.persistence.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarMecanicosUseCase {

    private final UsuarioGateway usuarioGateway;
    private final UsuarioMapper usuarioMapper;

    public List<UsuarioOutput> execute() {

        return usuarioMapper.mapToOutput(
                usuarioGateway.findByRole(RoleEnum.MECANICO)
        );
    }
}
