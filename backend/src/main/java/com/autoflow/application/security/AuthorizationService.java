package com.autoflow.application.security;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.output.veiculo.VeiculoOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final ClienteAutenticadoService clienteAutenticadoService;

    public void validarPermissao(VeiculoOutput veiculo) {
        Long clienteId = clienteAutenticadoService.getClienteId().orElse(null);

        if (clienteId != null && !veiculo.clienteId().equals(clienteId)) {
            throw ApplicationException.forbidden(
                    "Você não tem permissão para acessar este veículo.");
        }
    }
}
