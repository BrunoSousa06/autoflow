package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class BuscarPecaInsumoPorIdUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper pecaInsumoMapper;

    public PecaInsumoOutput execute(Long id) {
        PecaInsumoEntity pecaInsumoEntity = pecaInsumoGateway.findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Peça/Insumo não encontrado com o ID: " + id));


        return pecaInsumoMapper.mapToOutput(pecaInsumoEntity);

    }
}
