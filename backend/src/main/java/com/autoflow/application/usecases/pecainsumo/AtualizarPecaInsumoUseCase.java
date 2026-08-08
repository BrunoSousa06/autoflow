package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.mapper.PecaInsumoMapper;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AtualizarPecaInsumoUseCase {

    private final BuscarEAtualizarPecaInsumoPorIdUseCase buscarEAtualizarPecaInsumoPorIdUseCase;
    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper mapper;

    public PecaInsumoOutput execute(
            Long id,
            PecaInsumoInput request) {

        PecaInsumoEntity entity = buscarEAtualizarPecaInsumoPorIdUseCase.execute(id);

        mapper.updateEntity(request, entity);
        pecaInsumoGateway.save(entity);

        return mapper.mapToOutput(entity);
    }
}
