package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.PecaInsumoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DeletarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    public void execute(Long id) {

        if (!pecaInsumoGateway.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Peça/Insumo não encontrado com o ID: " + id);
        }

        pecaInsumoGateway.deleteById(id);
    }
}
