package com.autoflow.application.usecases.servico;

import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.application.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


@Component
@RequiredArgsConstructor
public class InativarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public void execute(Long id) {
        ServicoEntity entity = servicoGateway.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Serviço não encontrado com o ID: " + id));

        entity.setAtivo(false);
        servicoGateway.save(entity);
    }

}
