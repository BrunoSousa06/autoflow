package com.autoflow.presentation.servico.response;

public record TempoMedioServicoResponse(
        Long servicoId,
        String nomeServico,
        Long quantidadeExecucoes,
        Double tempoMedioSegundos,
        Double tempoMedioMinutos,
        Double tempoMedioHoras
) {
}