package com.autoflow.application.port.in.servico;

import com.autoflow.application.dto.servico.PageInput;
import com.autoflow.application.dto.servico.PageOutput;
import com.autoflow.application.dto.servico.ServicoOutput;

public interface ListarServicosUseCase {
    PageOutput<ServicoOutput> execute(PageInput page);
}
