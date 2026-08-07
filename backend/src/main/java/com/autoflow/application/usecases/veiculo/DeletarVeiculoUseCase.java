package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.gateway.VeiculoGateway;
import com.autoflow.application.exception.VeiculoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
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
