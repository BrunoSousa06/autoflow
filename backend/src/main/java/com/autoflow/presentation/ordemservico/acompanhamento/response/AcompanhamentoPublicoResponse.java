package com.autoflow.presentation.ordemservico.acompanhamento.response;

import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoPublicoOutput;

import java.time.LocalDateTime;

public record AcompanhamentoPublicoResponse(
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime dataAbertura,
        LocalDateTime execucaoIniciadaEm,
        LocalDateTime finalizadaEm,
        LocalDateTime entregueEm,
        Long orcamentoId
) {

    public static AcompanhamentoPublicoResponse from(
            AcompanhamentoPublicoOutput resultado
    ) {
        return new AcompanhamentoPublicoResponse(
                resultado.numeroOs(),
                resultado.status(),
                resultado.dataAbertura(),
                resultado.execucaoIniciadaEm(),
                resultado.finalizadaEm(),
                resultado.entregueEm(),
                resultado.orcamentoId()
        );
    }
}
