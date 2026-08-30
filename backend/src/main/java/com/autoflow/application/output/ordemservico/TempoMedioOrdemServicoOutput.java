package com.autoflow.application.output.ordemservico;

public record TempoMedioOrdemServicoOutput(
        Long quantidadeOrdensFinalizadas,
        Double tempoMedioSegundos,
        Double tempoMedioMinutos,
        Double tempoMedioHoras
) {
}
