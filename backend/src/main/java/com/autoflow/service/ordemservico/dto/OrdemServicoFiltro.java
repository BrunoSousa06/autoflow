package com.autoflow.service.ordemservico.dto;

import com.autoflow.domain.ordemservico.StatusOrdemServico;

public record OrdemServicoFiltro(
        String cliente,
        String numeroOs,
        StatusOrdemServico status
) {
}
