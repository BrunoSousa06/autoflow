package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class BuscarServicoPorIdUseCase {

    private final ServicoGateway servicoGateway;

    public ServicoOutput execute(Long id) {
        return servicoGateway.findById(id)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Serviço não encontrado com o ID: " + id));
    }

}
