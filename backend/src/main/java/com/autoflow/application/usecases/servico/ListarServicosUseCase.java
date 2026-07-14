package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.mapper.ServicoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListarServicosUseCase {

    private final ServicoGateway servicoGateway;
    private final ServicoMapper servicoMapper;

    public Page<ServicoOutput> execute(Pageable pageable) {
        return servicoGateway.findAllByAtivoTrue(pageable)
                .map(servicoMapper::mapToOutput);
    }

}
