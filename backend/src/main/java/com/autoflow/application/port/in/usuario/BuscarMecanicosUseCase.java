package com.autoflow.application.port.in.usuario;

import com.autoflow.application.dto.usuario.UsuarioOutput;

import java.util.List;

public interface BuscarMecanicosUseCase {
    List<UsuarioOutput> execute();
}
