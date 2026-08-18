package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.input.PageQuery;
import com.autoflow.application.output.PageResult;
import com.autoflow.application.input.pecainsumo.PecaInsumoFiltro;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.port.in.pecainsumo.ListarPecaInsumoPaginadoUseCase;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarPecaInsumoPaginadoUseCaseImpl implements ListarPecaInsumoPaginadoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    @Override
    public PageResult<PecaInsumoOutput> execute(
            PageQuery pageQuery,
            String nome,
            CategoriaPecaInsumo tipo) {
        return pecaInsumoGateway.findAll(new PecaInsumoFiltro(nome, tipo), pageQuery);
    }
}
