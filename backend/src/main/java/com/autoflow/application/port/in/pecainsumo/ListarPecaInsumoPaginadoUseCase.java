package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.application.input.PageQuery;
import com.autoflow.application.output.PageResult;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

public interface ListarPecaInsumoPaginadoUseCase {
    PageResult<PecaInsumoOutput> execute(PageQuery pageQuery, String nome, CategoriaPecaInsumo tipo);
}
