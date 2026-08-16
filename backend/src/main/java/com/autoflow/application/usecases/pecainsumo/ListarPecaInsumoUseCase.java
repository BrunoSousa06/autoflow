package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ListarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    public List<PecaInsumoOutput> execute() {
        return pecaInsumoGateway.findAll();
    }
}
