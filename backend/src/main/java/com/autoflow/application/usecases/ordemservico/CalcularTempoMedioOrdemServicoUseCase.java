package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.dto.ordemservico.TempoMedioOrdemServicoOutput;
import com.autoflow.application.gateway.MetricsGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CalcularTempoMedioOrdemServicoUseCase {

    private final MetricsGateway metricsGateway;

    public TempoMedioOrdemServicoOutput execute() {
        MetricsGateway.TempoMedioOrdemServicoData metrica =
                metricsGateway.calcularTempoMedioOrdensServico();
        Double segundos = metrica.tempoMedioSegundos();

        return new TempoMedioOrdemServicoOutput(
                metrica.quantidadeOrdensFinalizadas(),
                segundos,
                segundos == null ? null : segundos / 60,
                segundos == null ? null : segundos / 3600
        );
    }
}
