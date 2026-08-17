package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.port.in.pecainsumo.CadastrarPecaInsumoUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarPecaInsumoUseCaseImpl implements CadastrarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    @Override
    public PecaInsumoOutput execute(PecaInsumoInput request) {

        if (pecaInsumoGateway.findByNomeIgnoreCase(request.nome()).isPresent()) {
            throw ApplicationException.badRequest("Peça/Insumo já foi cadastrado");
        }
        return pecaInsumoGateway.save(request);
    }
}
