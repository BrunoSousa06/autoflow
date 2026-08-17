package com.autoflow.application.port.in.usuario;

import com.autoflow.domain.usuario.Usuario;

public interface BuscarUsuarioPorEmailUseCase {
    Usuario execute(String email);
}
