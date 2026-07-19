package com.autoflow.presentation.ordemservico.acompanhamento.response;

import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record HistoricoStatusOsResponse(
        StatusOrdemServico status,
        String mensagemCliente,
        LocalDateTime registradoEm
) {
    public static HistoricoStatusOsResponse from(HistoricoStatusOsEntity historico) {
        return new HistoricoStatusOsResponse(
                historico.getStatus(),
                historico.getMensagemCliente(),
                historico.getRegistradoEm()
        );
    }
}
