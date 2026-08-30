package com.autoflow.application.output.ordemservico.acompanhamento;

import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record HistoricoStatusOsOutput(
        StatusOrdemServico status,
        String mensagemCliente,
        LocalDateTime registradoEm
) {
    public static HistoricoStatusOsOutput from(HistoricoStatusOs historico) {
        return new HistoricoStatusOsOutput(
                historico.getStatus(), historico.getMensagemCliente(), historico.getRegistradoEm());
    }
}
