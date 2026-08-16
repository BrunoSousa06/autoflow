package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    public PecaInsumoOutput execute(PecaInsumoInput request) {

        if (pecaInsumoGateway.findByNomeIgnoreCase(request.nome()).isPresent()) {
            throw ApplicationException.badRequest("Peça/Insumo já foi cadastrado");
        }
        return pecaInsumoGateway.save(request);
    }
}
