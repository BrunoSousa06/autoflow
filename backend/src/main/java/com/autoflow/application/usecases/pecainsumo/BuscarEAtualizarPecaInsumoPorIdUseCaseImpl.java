package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.port.in.pecainsumo.BuscarEAtualizarPecaInsumoPorIdUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarEAtualizarPecaInsumoPorIdUseCaseImpl implements BuscarEAtualizarPecaInsumoPorIdUseCase {


    private final PecaInsumoGateway pecaInsumoGateway;

    @Override
    public PecaInsumoOutput execute(Long id) {

        return pecaInsumoGateway.findById(id).orElseThrow(() ->
                ApplicationException.notFound(
                        "Peça/Insumo não encontrado com o ID: " + id));

    }

}
