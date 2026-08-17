package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.domain.ordemservico.OrdemServico;

public interface ListarOrdensServicoUseCase {
    PageResult<OrdemServico> execute(
            OrdemServicoFiltroInput filtro,
            PageQuery pageQuery,
            String emailUsuarioLogado);
}
