package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.exception.VeiculoNaoEncontradoException;
import com.autoflow.application.gateway.VeiculoGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeletarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public void execute(Long id) {
        if (!veiculoGateway.existsById(id)) {
            throw new VeiculoNaoEncontradoException("Veículo não encontrado com o ID: " + id);
        }

        veiculoGateway.deleteById(id);
    }
}
