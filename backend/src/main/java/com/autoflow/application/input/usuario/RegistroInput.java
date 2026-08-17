package com.autoflow.application.input.usuario;

import com.autoflow.domain.usuario.RoleEnum;

public record RegistroInput(
        String nome,
        String email,
        String cpfCnpj,
        String telefone,
        String senha,
        RoleEnum role
) {

    public RegistroInput {
        if (role == null) {
            role = RoleEnum.CLIENTE;
        }
    }
}
