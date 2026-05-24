package com.autoflow.ordemServico.web;

import com.autoflow.ordemServico.domain.OrdemServico;
import com.autoflow.ordemServico.domain.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrdemServicoResponse(
        UUID id,
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime dataAbertura
) {

    static OrdemServicoResponse fromDomain(OrdemServico ordemServico) {
        return new OrdemServicoResponse(
                ordemServico.getId(),
                ordemServico.getNumeroOs(),
                ordemServico.getStatus(),
                ordemServico.getDataAbertura()
        );
    }
}
