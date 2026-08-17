package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.port.in.pecainsumo.BuscarPecaInsumoPorIdUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarPecaInsumoPorIdUseCaseImpl implements BuscarPecaInsumoPorIdUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    @Override
    public PecaInsumoOutput execute(Long id) {
        return pecaInsumoGateway.findById(id).orElseThrow(() ->
                ApplicationException.notFound(
                        "Peça/Insumo não encontrado com o ID: " + id));

    }
}
