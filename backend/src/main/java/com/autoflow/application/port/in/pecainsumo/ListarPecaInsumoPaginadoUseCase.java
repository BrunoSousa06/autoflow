package com.autoflow.application.port.in.pecainsumo;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

public interface ListarPecaInsumoPaginadoUseCase {
    PageResult<PecaInsumoOutput> execute(PageQuery pageQuery, String nome, CategoriaPecaInsumo tipo);
}
