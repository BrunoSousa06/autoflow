package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.mapper.PecaInsumoMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class ListarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper mapper;

    public List<PecaInsumoOutput> execute() {
        return pecaInsumoGateway.findAll().stream().map(mapper::mapToOutput).toList();
    }
}
