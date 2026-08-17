package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.input.PageQuery;
import com.autoflow.application.output.PageResult;
import com.autoflow.application.input.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.domain.ordemservico.OrdemServico;

public interface ListarOrdensServicoUseCase {
    PageResult<OrdemServico> execute(
            OrdemServicoFiltroInput filtro,
            PageQuery pageQuery,
            String emailUsuarioLogado);
}
