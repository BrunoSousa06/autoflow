package com.autoflow.application.port.in.usuario;

import com.autoflow.domain.usuario.Usuario;

public interface BuscarMecanicoPorIdUseCase {
    Usuario execute(Long mecanicoId);
}
