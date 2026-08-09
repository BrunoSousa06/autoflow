package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.mapper.PecaInsumoMapper;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarPecaInsumoPorIdUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper pecaInsumoMapper;

    public PecaInsumoOutput execute(Long id) {
        PecaInsumoEntity pecaInsumoEntity = pecaInsumoGateway.findById(id).orElseThrow(() ->
                ApplicationException.notFound(
                        "Peça/Insumo não encontrado com o ID: " + id));


        return pecaInsumoMapper.mapToOutput(pecaInsumoEntity);

    }
}
