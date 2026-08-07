package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.security.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BuscarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final AuthorizationService authorizationService;

    public VeiculoOutput execute(Long id) {
        VeiculoOutput veiculo = veiculoGateway.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Veículo não encontrado com o ID: " + id));

        authorizationService.validarPermissao(veiculo);
        return veiculo;
    }
}
