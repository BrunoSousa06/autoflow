package com.autoflow.application.usecases.servico;

import com.autoflow.application.input.servico.PageInput;
import com.autoflow.application.output.servico.PageOutput;
import com.autoflow.application.output.servico.ServicoOutput;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import com.autoflow.application.port.in.servico.ListarServicosUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ListarServicosUseCaseImpl implements ListarServicosUseCase {

    private final ServicoGateway servicoGateway;

    @Override
    public PageOutput<ServicoOutput> execute(PageInput page) {
        var result = servicoGateway.findAllByAtivoTrue(page);
        return new PageOutput<>(result.content().stream()
                .map(ServicoApplicationMapper::toOutput)
                .toList(), result.page(), result.size(), result.totalElements());
    }

}
