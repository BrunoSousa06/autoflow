package com.autoflow.application.security;

import com.autoflow.application.dto.veiculo.VeiculoOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ClienteAutenticadoService clienteAutenticadoService;

    public void validarPermissao(VeiculoOutput veiculo) {
        Long clienteId = clienteAutenticadoService.getClienteId().orElse(null);

        if (clienteId != null && !veiculo.clienteId().equals(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar este veículo.");
        }
    }
}
