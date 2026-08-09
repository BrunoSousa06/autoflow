package com.autoflow.presentation.orcamento.response;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record OrcamentoResponse(
        Long id,
        Long ordemServicoId,
        String numeroOs,
        TipoOrcamento tipo,
        Integer versao,
        StatusOrcamento status,
        BigDecimal totalServicos,
        BigDecimal totalItens,
        BigDecimal totalGeral,
        List<OrcamentoServicoResponse> servicos,
        List<OrcamentoItemNecessarioResponse> itens,
        LocalDateTime criadoEm,
        LocalDateTime disponibilizadoEm
) {
    public static OrcamentoResponse from(OrcamentoEntity orcamentoEntity) {
        return new OrcamentoResponse(orcamentoEntity.getId(),
                orcamentoEntity.getOrdemServicoId(),
                orcamentoEntity.getNumeroOs(),
                orcamentoEntity.getTipo(),
                orcamentoEntity.getVersao(),
                orcamentoEntity.getStatus(),
                orcamentoEntity.getTotalServicos(),
                orcamentoEntity.getTotalItens(),
                orcamentoEntity.getTotalGeral(),
                mapServicos(orcamentoEntity),
                mapItens(orcamentoEntity),
                orcamentoEntity.getCriadoEm(),
                orcamentoEntity.getDisponibilizadoEm());
    }

    private static List<OrcamentoServicoResponse> mapServicos(OrcamentoEntity orcamentoEntity) {
        if (orcamentoEntity.getServicos() == null) {
            return List.of();
        }

        return orcamentoEntity.getServicos()
                .stream()
                .filter(Objects::nonNull)
                .map(OrcamentoServicoResponse::from)
                .toList();
    }

    private static List<OrcamentoItemNecessarioResponse> mapItens(OrcamentoEntity orcamentoEntity) {
        if (orcamentoEntity.getItens() == null) {
            return List.of();
        }

        return orcamentoEntity.getItens()
                .stream()
                .filter(Objects::nonNull)
                .map(OrcamentoItemNecessarioResponse::from)
                .toList();
    }
}
