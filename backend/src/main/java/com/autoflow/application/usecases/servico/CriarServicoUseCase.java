package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CriarServicoUseCase {

    private final ServicoGateway servicoGateway;

    public ServicoOutput execute(ServicoInput input) {
        if (servicoGateway.existsByNomeIgnoreCase(input.nome())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço já foi cadastrado");
        }

        return servicoGateway.save(input);
    }

}
