package com.autoflow.application.usecases.servico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.input.servico.ServicoInput;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import com.autoflow.application.output.servico.ServicoOutput;
import com.autoflow.application.port.in.servico.CriarServicoUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CriarServicoUseCaseImpl implements CriarServicoUseCase {

    private final ServicoGateway servicoGateway;

    @Override
    public ServicoOutput execute(ServicoInput input) {
        if (servicoGateway.existsByNomeIgnoreCase(input.nome())) {
            throw ApplicationException.conflict("Serviço já foi cadastrado");
        }

        return ServicoApplicationMapper.toOutput(
                servicoGateway.save(ServicoApplicationMapper.toDomain(input)));
    }

}
