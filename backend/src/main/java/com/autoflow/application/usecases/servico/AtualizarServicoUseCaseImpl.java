package com.autoflow.application.usecases.servico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.input.servico.ServicoInput;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import com.autoflow.application.output.servico.ServicoOutput;
import com.autoflow.application.port.in.servico.AtualizarServicoUseCase;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AtualizarServicoUseCaseImpl implements AtualizarServicoUseCase {

    private final ServicoGateway servicoGateway;

    @Override
    public ServicoOutput execute(Long id, ServicoInput input) {
        var existente = servicoGateway.findById(id)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Serviço não encontrado com o ID: " + id));
        return ServicoApplicationMapper.toOutput(servicoGateway.update(
                ServicoApplicationMapper.toDomain(id, input, existente.ativo())));
    }

}
