package com.autoflow.application.security;

import com.autoflow.application.dto.security.CurrentUser;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UsuarioAutenticadoService {

    private final CurrentUserGateway currentUserGateway;

    public Optional<CurrentUser> getCurrentUser() {
        return currentUserGateway.getCurrentUser();
    }

    public String getEmail() {
        return getRequiredUser().email();
    }

    public boolean isCliente() {
        return hasRole(RoleEnum.CLIENTE);
    }

    public boolean isAdministrador() {
        return hasRole(RoleEnum.ADMIN);
    }

    private boolean hasRole(RoleEnum role) {
        return getCurrentUser()
                .map(user -> user.hasRole(role))
                .orElse(false);
    }

    private CurrentUser getRequiredUser() {
        return getCurrentUser().orElseThrow(() -> ApplicationException.unauthorized(
                "Usuário não autenticado"
        ));
    }
}
