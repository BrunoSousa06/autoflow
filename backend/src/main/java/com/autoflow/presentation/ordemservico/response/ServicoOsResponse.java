package com.autoflow.presentation.ordemservico.response;

import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.StatusServicoOs;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ServicoOsResponse(
        Long id,
        Long servicoId,
        String nome,
        BigDecimal valor,
        StatusServicoOs status,
        LocalDateTime iniciadoEm,
        LocalDateTime finalizadoEm,
        List<ItemNecessarioResponse> itensNecessarios
) {
    public static ServicoOsResponse fromDomain(ServicoSolicitado servicoSolicitadoEntity) {
        return new ServicoOsResponse(
                servicoSolicitadoEntity.getId(),
                servicoSolicitadoEntity.getServicoId(),
                servicoSolicitadoEntity.getNome(),
                servicoSolicitadoEntity.getValor(),
                servicoSolicitadoEntity.getStatus(),
                servicoSolicitadoEntity.getIniciadoEm(),
                servicoSolicitadoEntity.getFinalizadoEm(),
                servicoSolicitadoEntity.getItensNecessarios().stream()
                        .map(ItemNecessarioResponse::fromDomain)
                        .toList()
        );
    }
}
