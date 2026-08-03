package com.autoflow.application.dto.ordemservico;

public record TempoMedioOrdemServicoOutput(
        Long quantidadeOrdensFinalizadas,
        Double tempoMedioSegundos,
        Double tempoMedioMinutos,
        Double tempoMedioHoras
) {
}
