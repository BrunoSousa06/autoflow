package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.port.in.pecainsumo.ListarPecaInsumoUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ListarPecaInsumoUseCaseImpl implements ListarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    @Override
    public List<PecaInsumoOutput> execute() {
        return pecaInsumoGateway.findAll();
    }
}
