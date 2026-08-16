package com.autoflow.application.usecases.veiculo.fixture;

import org.springframework.security.core.context.SecurityContextHolder;

public class IllegalVeiculoApplicationSecurityDependency {
    public Object currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
