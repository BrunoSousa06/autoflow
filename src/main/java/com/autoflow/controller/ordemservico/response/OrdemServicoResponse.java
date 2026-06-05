package com.autoflow.controller.ordemservico.response;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.List;


public record OrdemServicoResponse(
        Long id,
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime dataAbertura,
        LocalDateTime execucaoIniciadaEm,
        LocalDateTime finalizadaEm,
        List<ServicoOsResponse> servicos
) {
    public static OrdemServicoResponse fromDomain(OrdemServicoEntity os) {
        return new OrdemServicoResponse(
                os.getId(),
                os.getNumeroOs(),
                os.getStatus(),
                os.getDataAbertura(),
                os.getExecucaoIniciadaEm(),
                os.getFinalizadaEm(),
                os.getServicosSolicitados().stream()
                        .map(ServicoOsResponse::fromDomain)
                        .toList()
        );
    }
}
