package com.autoflow.application.dto.ordemservico.acompanhamento;

import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record AcompanhamentoPublicoOutput(
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime dataAbertura,
        LocalDateTime execucaoIniciadaEm,
        LocalDateTime finalizadaEm,
        LocalDateTime entregueEm,
        Long orcamentoId
) {
}
