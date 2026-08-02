package com.autoflow.service.ordemservico.dto;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;

public record OrdemServicoCriada(
        OrdemServicoEntity ordemServico,
        String tokenAcompanhamento
) {
}