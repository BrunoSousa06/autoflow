package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.mapper.ServicoApplicationMapper;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class AtualizarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public ServicoOutput execute(Long id, ServicoInput input) {
        var existente = servicoGateway.findById(id)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Serviço não encontrado com o ID: " + id));
        return ServicoApplicationMapper.toOutput(servicoGateway.update(
                ServicoApplicationMapper.toDomain(id, input, existente.ativo())));
    }

}
