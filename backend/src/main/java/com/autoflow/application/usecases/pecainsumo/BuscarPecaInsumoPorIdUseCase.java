package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarPecaInsumoPorIdUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    public PecaInsumoOutput execute(Long id) {
        return pecaInsumoGateway.findById(id).orElseThrow(() ->
                ApplicationException.notFound(
                        "Peça/Insumo não encontrado com o ID: " + id));

    }
}
