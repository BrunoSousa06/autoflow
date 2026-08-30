package com.autoflow.presentation.usuario.response;

import com.autoflow.domain.usuario.RoleEnum;

public record UsuarioCadastroResponse(
        Long id,
        String nome,
        String email,
        RoleEnum role) {
}
