package com.autoflow.application.dto.ordemservico;

import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record StatusOrdemServicoOutput(
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime ultimaAtualizacao
) {
}
