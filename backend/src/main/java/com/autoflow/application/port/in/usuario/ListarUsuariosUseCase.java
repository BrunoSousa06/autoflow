package com.autoflow.application.port.in.usuario;

import com.autoflow.application.output.usuario.UsuarioOutput;

import java.util.List;

public interface ListarUsuariosUseCase {
    List<UsuarioOutput> execute();
}
