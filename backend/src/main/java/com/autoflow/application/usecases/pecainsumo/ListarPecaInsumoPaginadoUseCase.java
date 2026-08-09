package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.pecainsumo.PecaInsumoFiltro;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.mapper.PecaInsumoMapper;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarPecaInsumoPaginadoUseCase {

    private final PecaInsumoGateway pecaInsumoGateway;
    private final PecaInsumoMapper mapper;

    public PageResult<PecaInsumoOutput> execute(
            PageQuery pageQuery,
            String nome,
            CategoriaPecaInsumo tipo) {
        var page = pecaInsumoGateway.findAll(new PecaInsumoFiltro(nome, tipo), pageQuery);
        return new PageResult<>(page.content().stream().map(mapper::mapToOutput).toList(),
                page.totalElements(), page.page(), page.size());
    }
}
