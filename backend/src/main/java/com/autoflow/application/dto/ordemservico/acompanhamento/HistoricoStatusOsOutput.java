package com.autoflow.application.dto.ordemservico.acompanhamento;

import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record HistoricoStatusOsOutput(
        StatusOrdemServico status,
        String mensagemCliente,
        LocalDateTime registradoEm
) {
    public static HistoricoStatusOsOutput from(HistoricoStatusOsEntity historico) {
        return new HistoricoStatusOsOutput(
                historico.getStatus(), historico.getMensagemCliente(), historico.getRegistradoEm());
    }
}
