package com.autoflow.application.port.in.servico;

import com.autoflow.application.input.servico.PageInput;
import com.autoflow.application.output.servico.PageOutput;
import com.autoflow.application.output.servico.ServicoOutput;

public interface ListarServicosUseCase {
    PageOutput<ServicoOutput> execute(PageInput page);
}
