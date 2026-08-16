package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.PageInput;
import com.autoflow.application.dto.servico.PageOutput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarServicosUseCase {

    private final ServicoGateway servicoGateway;

    public PageOutput<ServicoOutput> execute(PageInput page) {
        var result = servicoGateway.findAllByAtivoTrue(page);
        return new PageOutput<>(result.content().stream()
                .map(ServicoApplicationMapper::toOutput)
                .toList(), result.page(), result.size(), result.totalElements());
    }

}
