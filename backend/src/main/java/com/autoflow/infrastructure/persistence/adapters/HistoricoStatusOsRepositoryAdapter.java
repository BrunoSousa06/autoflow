package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.repository.ordemservico.historico.HistoricoStatusOsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HistoricoStatusOsRepositoryAdapter implements HistoricoStatusOsGateway {

    private final HistoricoStatusOsRepository repository;

    @Override
    public HistoricoStatusOsEntity save(HistoricoStatusOsEntity historico) {
        return repository.save(historico);
    }

    @Override
    public List<HistoricoStatusOsEntity> findByOrdemServicoIdOrderByRegistradoEmAsc(
            Long ordemServicoId) {

        return repository.findByOrdemServicoIdOrderByRegistradoEmAsc(ordemServicoId);
    }

    @Override
    public List<HistoricoStatusOsEntity> findByNumeroOsOrderByRegistradoEmAsc(
            String numeroOs) {

        return repository.findByNumeroOsOrderByRegistradoEmAsc(numeroOs);
    }
}
