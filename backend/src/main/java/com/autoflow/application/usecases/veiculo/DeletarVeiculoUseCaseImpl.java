package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.exception.VeiculoNaoEncontradoException;
import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.port.in.veiculo.DeletarVeiculoUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeletarVeiculoUseCaseImpl implements DeletarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    @Override
    public void execute(Long id) {
        if (!veiculoGateway.existsById(id)) {
            throw new VeiculoNaoEncontradoException("Veículo não encontrado com o ID: " + id);
        }

        veiculoGateway.deleteById(id);
    }
}
