package com.autoflow.application.usecases.servico;

import com.autoflow.application.output.servico.ServicoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import com.autoflow.application.port.in.servico.BuscarServicoPorIdUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarServicoPorIdUseCaseImpl implements BuscarServicoPorIdUseCase {

    private final ServicoGateway servicoGateway;

    @Override
    public ServicoOutput execute(Long id) {
        return servicoGateway.findById(id)
                .map(ServicoApplicationMapper::toOutput)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Serviço não encontrado com o ID: " + id));
    }

}
