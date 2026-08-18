package com.autoflow.application.usecases.servico;

import com.autoflow.application.output.servico.TempoMedioServicoMetricaOutput;
import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.application.port.in.servico.CalcularTempoMedioServicoUseCase;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class CalcularTempoMedioServicoUseCaseImpl implements CalcularTempoMedioServicoUseCase {

    private final MetricsGateway metricsGateway;

    @Override
    public List<TempoMedioServicoMetricaOutput> execute() {
        return metricsGateway.calcularTempoMedioPorServico()
                .stream()
                .map(this::mapToOutput)
                .toList();
    }

    private TempoMedioServicoMetricaOutput mapToOutput(MetricsGateway.TempoMedioServicoData metrica) {
        Double tempoMedioSegundos = metrica.tempoMedioSegundos();

        return TempoMedioServicoMetricaOutput.builder()
                .servicoId(metrica.servicoId())
                .nomeServico(metrica.nomeServico())
                .quantidadeExecucoes(metrica.quantidadeExecucoes())
                .tempoMedioSegundos(tempoMedioSegundos)
                .tempoMedioMinutos(tempoMedioSegundos == null ? null : tempoMedioSegundos / 60)
                .tempoMedioHoras(tempoMedioSegundos == null ? null : tempoMedioSegundos / 3600)
                .build();
    }

}
