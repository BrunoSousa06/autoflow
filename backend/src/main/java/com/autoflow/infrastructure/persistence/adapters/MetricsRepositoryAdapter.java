package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.infrastructure.persistence.repository.ServicoSolicitadoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MetricsRepositoryAdapter implements MetricsGateway {

    private final OrdemServicoRepository ordemServicoRepository;
    private final ServicoSolicitadoRepository servicoSolicitadoRepository;

    @Override
    public TempoMedioOrdemServicoData calcularTempoMedioOrdensServico() {
        var projection = ordemServicoRepository.calcularTempoMedioFinalizacao();
        return new TempoMedioOrdemServicoData(
                projection.getQuantidadeOrdensFinalizadas(),
                projection.getTempoMedioSegundos()
        );
    }

    @Override
    public List<TempoMedioServicoData> calcularTempoMedioPorServico() {
        return servicoSolicitadoRepository.calcularTempoMedioPorServico()
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
