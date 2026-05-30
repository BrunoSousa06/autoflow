package com.autoflow.controller.usuario.request;

import com.autoflow.domain.usuario.RoleEnum;

public record RegistroRequest(
        String nome,
        String email,
        String cpfCnpj,
        String telefone,
        String senha,
        RoleEnum role
) {
}
