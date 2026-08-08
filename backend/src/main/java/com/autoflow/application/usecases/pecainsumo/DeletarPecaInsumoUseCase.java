package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeletarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    public void execute(Long id) {

        if (!pecaInsumoGateway.existsById(id)) {
            throw ApplicationException.notFound("Peça/Insumo não encontrado com o ID: " + id);
        }

        pecaInsumoGateway.deleteById(id);
    }
}
