package com.autoflow.infrastructure.security;

import com.autoflow.application.output.security.CurrentUser;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.domain.usuario.RoleEnum;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CurrentUserGatewayAdapter implements CurrentUserGateway {

    @Override
    public Optional<CurrentUser> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Optional<RoleEnum> role = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .flatMap(value -> Arrays.stream(RoleEnum.values())
                        .filter(candidate -> candidate.name().equals(value)))
                .findFirst();

        return role.map(value -> new CurrentUser(authentication.getName(), value));
    }
}
