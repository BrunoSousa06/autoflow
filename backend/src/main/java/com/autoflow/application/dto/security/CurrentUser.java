package com.autoflow.application.dto.security;

import com.autoflow.domain.usuario.RoleEnum;

public record CurrentUser(String email, RoleEnum role) {

    public boolean hasRole(RoleEnum expectedRole) {
        return expectedRole.equals(role);
    }
}
