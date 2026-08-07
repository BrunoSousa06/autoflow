package com.autoflow.application.security;

import com.autoflow.application.dto.security.CurrentUser;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteAutenticadoService {

    private final CurrentUserGateway currentUserGateway;
    private final VeiculoClienteGateway clienteGateway;

    public Optional<Long> getClienteId() {
        CurrentUser currentUser = currentUserGateway.getCurrentUser()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário não autenticado"));

        if (!currentUser.hasRole(RoleEnum.CLIENTE)) {
            return Optional.empty();
        }

        Long clienteId = clienteGateway.findIdByUsuarioEmail(currentUser.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Cliente não encontrado para o usuário autenticado"));

        return Optional.of(clienteId);
    }
}
