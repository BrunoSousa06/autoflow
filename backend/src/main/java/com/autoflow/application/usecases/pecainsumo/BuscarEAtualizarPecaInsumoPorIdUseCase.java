package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarEAtualizarPecaInsumoPorIdUseCase {


    private final PecaInsumoGateway pecaInsumoGateway;

    public PecaInsumoEntity execute(Long id) {

        return pecaInsumoGateway.findById(id).orElseThrow(() ->
                ApplicationException.notFound(
                        "Peça/Insumo não encontrado com o ID: " + id));

    }

}
