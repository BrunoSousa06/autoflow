package com.autoflow.application.dto.usuario;

import com.autoflow.domain.usuario.RoleEnum;
import lombok.Builder;

@Builder
public record UsuarioOutput(Long id,
                            String nome,
                            String email,
                            RoleEnum role) {
}
