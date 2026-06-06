package com.autoflow.domain.usuario;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RoleEnum {
    ADMIN,
    MECANICO,
    CLIENTE,
    ATENDENTE;


    @JsonCreator
    public static RoleEnum fromValue(String value) {

        for (RoleEnum role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }

        throw new IllegalArgumentException(
                "Role inválida. Valores permitidos: CLIENTE, MECANICO, ADMIN e ATENDENTE "
        );
    }
}
