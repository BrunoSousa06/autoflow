package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.infrastructure.persistence.mapper.ServicoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CriarServicoUseCase {

    private final ServicoGateway servicoGateway;
    private final ServicoMapper servicoMapper;

    public ServicoOutput execute(ServicoInput input) {
        // Validate duplicate name
        if (servicoGateway.findByNomeIgnoreCase(input.nome()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Serviço já foi cadastrado");
        }

        // Create and save entity
        ServicoEntity entity = new ServicoEntity();
        entity.setNome(input.nome());
        entity.setDescricao(input.descricao());
        entity.setValor(input.valor());
        entity.setAtivo(true);

        ServicoEntity saved = servicoGateway.save(entity);

        return servicoMapper.mapToOutput(saved);
    }

}
