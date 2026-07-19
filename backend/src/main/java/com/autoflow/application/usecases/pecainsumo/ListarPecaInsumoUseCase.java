package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoMapper;
import com.autoflow.presentation.pecainsumo.response.PecaInsumoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarPecaInsumoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper mapper;

    public List<PecaInsumoResponse> execute() {
        return mapper.toResponseList(pecaInsumoGateway.findAll());
    }
}
