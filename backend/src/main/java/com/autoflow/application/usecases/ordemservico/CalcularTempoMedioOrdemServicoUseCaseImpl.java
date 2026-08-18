package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.output.ordemservico.TempoMedioOrdemServicoOutput;
import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.application.port.in.ordemservico.CalcularTempoMedioOrdemServicoUseCase;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CalcularTempoMedioOrdemServicoUseCaseImpl implements CalcularTempoMedioOrdemServicoUseCase {

    private final MetricsGateway metricsGateway;

    @Override
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
