package com.autoflow.controller.ordemservico.response;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;


public record OrdemServicoResponse(
        Long id,
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime dataAbertura
) {

    public static OrdemServicoResponse fromDomain(OrdemServicoEntity ordemServicoEntity) {
        return new OrdemServicoResponse(
                ordemServicoEntity.getId(),
                ordemServicoEntity.getNumeroOs(),
                ordemServicoEntity.getStatus(),
                ordemServicoEntity.getDataAbertura()
        );
    }
}
