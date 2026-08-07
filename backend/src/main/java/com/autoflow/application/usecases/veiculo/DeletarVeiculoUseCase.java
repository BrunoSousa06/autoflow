package com.autoflow.application.usecases.veiculo;

import com.autoflow.application.gateway.VeiculoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DeletarVeiculoUseCase {

    private final VeiculoGateway veiculoGateway;

    public void execute(Long id) {
        if (!veiculoGateway.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Veículo não encontrado com o ID: " + id);
        }

        veiculoGateway.deleteById(id);
    }
}
