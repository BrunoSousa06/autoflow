package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.infrastructure.persistence.mapper.ordemservico.HistoricoStatusOsPersistenceMapper;
import com.autoflow.infrastructure.persistence.repository.historico.HistoricoStatusOsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HistoricoStatusOsRepositoryAdapter implements HistoricoStatusOsGateway {

    private final HistoricoStatusOsRepository repository;
    private final HistoricoStatusOsPersistenceMapper mapper;

    @Override
    public HistoricoStatusOs save(HistoricoStatusOs historico) {
        return mapper.toDomain(repository.save(mapper.toEntity(historico)));
    }

    @Override
    public List<HistoricoStatusOs> findByOrdemServicoIdOrderByRegistradoEmAsc(
            Long ordemServicoId) {

        return repository.findByOrdemServicoIdOrderByRegistradoEmAsc(ordemServicoId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<HistoricoStatusOs> findByNumeroOsOrderByRegistradoEmAsc(
            String numeroOs) {

        return repository.findByNumeroOsOrderByRegistradoEmAsc(numeroOs).stream().map(mapper::toDomain).toList();
    }
}
