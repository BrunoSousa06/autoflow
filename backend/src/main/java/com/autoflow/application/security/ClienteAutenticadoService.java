package com.autoflow.application.security;

import com.autoflow.application.exception.ClienteAutenticadoNaoEncontradoException;
import com.autoflow.application.exception.UsuarioNaoAutenticadoException;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.output.security.CurrentUser;
import com.autoflow.domain.usuario.RoleEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteAutenticadoService {

    private final CurrentUserGateway currentUserGateway;
    private final VeiculoClienteGateway clienteGateway;

    public Optional<Long> getClienteId() {
        CurrentUser currentUser = currentUserGateway.getCurrentUser()
                .orElseThrow(() -> new UsuarioNaoAutenticadoException(
                        "Usuário não autenticado"));

        if (!currentUser.hasRole(RoleEnum.CLIENTE)) {
            return Optional.empty();
        }

        Long clienteId = clienteGateway.findIdByUsuarioEmail(currentUser.email())
                .orElseThrow(() -> new ClienteAutenticadoNaoEncontradoException(
                        "Cliente não encontrado para o usuário autenticado"));

        return Optional.of(clienteId);
    }
}
