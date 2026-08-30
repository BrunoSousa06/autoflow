package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.infrastructure.persistence.repository.OrdemServicoRepository;
import com.autoflow.infrastructure.persistence.repository.ServicoSolicitadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MetricsRepositoryAdapter implements MetricsGateway {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ServicoSolicitadoRepository servicoSolicitadoRepository;

    @Override
    public TempoMedioOrdemServicoData calcularTempoMedioOrdensServico() {
        var projection = ordemServicoRepository.calcularTempoMedioFinalizacao();
        if (projection == null) {
            return new TempoMedioOrdemServicoData(0L, null);
        }
        return new TempoMedioOrdemServicoData(
                projection.getQuantidadeOrdensFinalizadas(),
                projection.getTempoMedioSegundos()
        );
    }

    @Override
    public List<TempoMedioServicoData> calcularTempoMedioPorServico() {
        var projections = servicoSolicitadoRepository.calcularTempoMedioPorServico();
        if (projections == null) {
            return Collections.emptyList();
        }
        return projections
                .stream()
                .map(projection -> new TempoMedioServicoData(
                        projection.getServicoId(),
                        projection.getNomeServico(),
                        projection.getQuantidadeExecucoes(),
                        projection.getTempoMedioSegundos()
                ))
                .toList();
    }
}
