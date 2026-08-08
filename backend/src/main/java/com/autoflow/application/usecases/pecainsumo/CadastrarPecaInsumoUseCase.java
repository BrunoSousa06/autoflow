package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.mapper.PecaInsumoMapper;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CadastrarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper mapper;

    public PecaInsumoOutput execute(PecaInsumoInput request) {

        if (pecaInsumoGateway.findByNomeIgnoreCase(request.nome()).isPresent()) {
            throw ApplicationException.badRequest("Peça/Insumo já foi cadastrado");
        }
        PecaInsumoEntity pecaInsumoEntity = mapper.mapToEntity(request);
        PecaInsumoEntity entity = pecaInsumoGateway.save(pecaInsumoEntity);


        return mapper.mapToOutput(entity);
    }
}
