package com.autoflow.presentation.ordemservico.response;

import com.autoflow.application.output.ordemservico.StatusOrdemServicoOutput;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record StatusOrdemServicoResponse(
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime ultimaAtualizacao
) {
    public static StatusOrdemServicoResponse from(StatusOrdemServicoOutput output) {
        return new StatusOrdemServicoResponse(
                output.numeroOs(),
                output.status(),
                output.ultimaAtualizacao());
    }
}
