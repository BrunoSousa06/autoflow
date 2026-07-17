package com.autoflow.application.security;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthorizationService {

    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final ClienteAutenticadoService clienteAutenticadoService;

    public void validarPermissao(VeiculoEntity veiculo) {

        if (!usuarioAutenticadoService.isCliente()) {
            return;
        }

        Long clienteId = clienteAutenticadoService.getClienteId();

        if (!veiculo.getCliente().getId().equals(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar este veículo.");
        }
    }

    public void validarPermissao(OrdemServicoEntity ordemServico) {

        if (!usuarioAutenticadoService.isCliente()) {
            return;
        }

        Long clienteId = clienteAutenticadoService.getClienteId();

        if (!ordemServico.getCliente().getId().equals(clienteId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Você não tem permissão para acessar esta ordem de serviço.");
        }
    }
}
