package com.autoflow.application.gateway;

import java.util.List;

public interface MetricsGateway {

    TempoMedioOrdemServicoData calcularTempoMedioOrdensServico();

    List<TempoMedioServicoData> calcularTempoMedioPorServico();

    record TempoMedioOrdemServicoData(
            Long quantidadeOrdensFinalizadas,
            Double tempoMedioSegundos
    ) {
    }

    record TempoMedioServicoData(
            Long servicoId,
            String nomeServico,
            Long quantidadeExecucoes,
            Double tempoMedioSegundos
    ) {
    }
}
