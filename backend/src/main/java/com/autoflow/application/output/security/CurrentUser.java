package com.autoflow.application.output.security;

import com.autoflow.domain.usuario.RoleEnum;

public record CurrentUser(String email, RoleEnum role) {

    public boolean hasRole(RoleEnum expectedRole) {
        return expectedRole.equals(role);
    }
}
