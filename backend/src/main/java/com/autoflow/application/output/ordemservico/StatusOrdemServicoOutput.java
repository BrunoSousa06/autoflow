package com.autoflow.application.output.ordemservico;

import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record StatusOrdemServicoOutput(
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime ultimaAtualizacao
) {
}
