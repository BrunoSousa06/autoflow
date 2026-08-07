package com.autoflow.application.security;

import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.application.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ClienteAutenticadoService {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ClienteGateway clienteGateway;

    public ClienteEntity getClienteLogado() {

        if (!usuarioAutenticadoService.isCliente()) {
            return null;
        }

        return clienteGateway
                .findByUsuarioEmail(usuarioAutenticadoService.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Cliente não encontrado para o usuário autenticado"));
    }

    public Long getClienteId() {

        ClienteEntity cliente = getClienteLogado();

        return cliente == null
                ? null
                : cliente.getId();
    }
}
