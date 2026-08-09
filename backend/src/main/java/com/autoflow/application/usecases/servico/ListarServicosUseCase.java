package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.PageInput;
import com.autoflow.application.dto.servico.PageOutput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarServicosUseCase {

    private final ServicoGateway servicoGateway;

    public PageOutput<ServicoOutput> execute(PageInput page) {
        return servicoGateway.findAllByAtivoTrue(page);
    }

}
