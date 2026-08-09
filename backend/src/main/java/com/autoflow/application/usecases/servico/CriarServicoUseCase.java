package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CriarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public ServicoOutput execute(ServicoInput input) {
        if (servicoGateway.existsByNomeIgnoreCase(input.nome())) {
            throw ApplicationException.conflict("Serviço já foi cadastrado");
        }

        return servicoGateway.save(input);
    }

}
