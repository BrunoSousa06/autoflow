package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
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
public class CadastrarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper mapper;

    public PecaInsumoOutput execute(PecaInsumoInput request) {

        if (pecaInsumoGateway.findByNomeIgnoreCase(request.nome()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Peça/Insumo já foi cadastrado");
        }
        PecaInsumoEntity pecaInsumoEntity = mapper.mapToEntity(request);
        PecaInsumoEntity entity = pecaInsumoGateway.save(pecaInsumoEntity);


        return mapper.mapToOutput(entity);
    }
}
