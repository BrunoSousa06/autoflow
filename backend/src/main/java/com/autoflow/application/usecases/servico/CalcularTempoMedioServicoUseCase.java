package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.TempoMedioServicoMetricaOutput;
import com.autoflow.infrastructure.persistence.repository.ServicoSolicitadoRepository;
import com.autoflow.infrastructure.persistence.repository.TempoMedioServicoProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CalcularTempoMedioServicoUseCase {

    private final ServicoSolicitadoRepository servicoSolicitadoRepository;

    public List<TempoMedioServicoMetricaOutput> execute() {
        return servicoSolicitadoRepository.calcularTempoMedioPorServico()
                .stream()
                .map(this::mapToOutput)
                .toList();
    }

    private TempoMedioServicoMetricaOutput mapToOutput(TempoMedioServicoProjection projection) {
        Double tempoMedioSegundos = projection.getTempoMedioSegundos();

        return TempoMedioServicoMetricaOutput.builder()
                .servicoId(projection.getServicoId())
                .nomeServico(projection.getNomeServico())
                .quantidadeExecucoes(projection.getQuantidadeExecucoes())
                .tempoMedioSegundos(tempoMedioSegundos)
                .tempoMedioMinutos(tempoMedioSegundos != null ? tempoMedioSegundos / 60 : 0.0)
                .tempoMedioHoras(tempoMedioSegundos != null ? tempoMedioSegundos / 3600 : 0.0)
                .build();
    }

}
