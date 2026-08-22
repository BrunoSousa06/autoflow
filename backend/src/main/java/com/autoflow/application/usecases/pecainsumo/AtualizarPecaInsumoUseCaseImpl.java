package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.input.pecainsumo.PecaInsumoInput;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.port.in.pecainsumo.AtualizarPecaInsumoUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AtualizarPecaInsumoUseCaseImpl implements AtualizarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    @Override
    public PecaInsumoOutput execute(
            Long id,
            PecaInsumoInput request) {

        pecaInsumoGateway.findById(id).orElseThrow(() ->
                ApplicationException.notFound(
                        "Peça/Insumo não encontrado com o ID: " + id));
        return pecaInsumoGateway.update(id, request);
    }
}
