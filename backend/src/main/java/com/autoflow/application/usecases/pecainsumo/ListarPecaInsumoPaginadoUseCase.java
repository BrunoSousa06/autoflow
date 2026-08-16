package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.pecainsumo.PecaInsumoFiltro;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarPecaInsumoPaginadoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;

    public PageResult<PecaInsumoOutput> execute(
            PageQuery pageQuery,
            String nome,
            CategoriaPecaInsumo tipo) {
        return pecaInsumoGateway.findAll(new PecaInsumoFiltro(nome, tipo), pageQuery);
    }
}
