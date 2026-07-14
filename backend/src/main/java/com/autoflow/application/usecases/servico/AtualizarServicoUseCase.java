package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.domain.servico.ServicoEntity;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.mapper.ServicoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class AtualizarServicoUseCase {

    private final ServicoGateway servicoGateway;
    private final ServicoMapper servicoMapper;

    public ServicoOutput execute(Long id, ServicoInput input) {
        ServicoEntity entity = servicoGateway.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Serviço não encontrado com o ID: " + id));

        // Update fields from input
        entity.setNome(input.nome());
        entity.setDescricao(input.descricao());
        entity.setValor(input.valor());

        ServicoEntity updated = servicoGateway.save(entity);

        return servicoMapper.mapToOutput(updated);
    }

}
