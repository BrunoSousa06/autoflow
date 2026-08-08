package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.dto.veiculo.VeiculoOutput;
import com.autoflow.application.exception.VeiculoNaoEncontradoException;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.security.AuthorizationService;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;
    private final AuthorizationService authorizationService;

    public VeiculoOutput execute(Long id) {
        VeiculoOutput veiculo = veiculoGateway.findById(id)
                .orElseThrow(() -> new VeiculoNaoEncontradoException(
                        "Veículo não encontrado com o ID: " + id));

        authorizationService.validarPermissao(veiculo);
        return veiculo;
    }
}
