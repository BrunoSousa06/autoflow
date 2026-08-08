package com.autoflow.application.usecases.servico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class InativarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public void execute(Long id) {
        servicoGateway.findById(id)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Serviço não encontrado com o ID: " + id));
        servicoGateway.inativar(id);
    }

}
