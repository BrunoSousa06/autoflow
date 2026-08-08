package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.application.gateway.ServicoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class BuscarServicoPorIdUseCase {

    private final ServicoGateway servicoGateway;

    public ServicoOutput execute(Long id) {
        return servicoGateway.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Serviço não encontrado com o ID: " + id));
    }

}
