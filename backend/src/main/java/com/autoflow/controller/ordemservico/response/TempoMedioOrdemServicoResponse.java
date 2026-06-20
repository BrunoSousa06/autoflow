package com.autoflow.controller.ordemservico.response;


public record TempoMedioOrdemServicoResponse(
        Long quantidadeOrdensFinalizadas,
        Double tempoMedioSegundos,
        Double tempoMedioMinutos,
        Double tempoMedioHoras
) {}