package com.autoflow.controller.orcamento.response;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrcamentoPublicoResponse(
        Long id,
        Long ordemServicoId,
        String numeroOs,
        TipoOrcamento tipo,
        Integer versao,
        StatusOrcamento status,
        BigDecimal totalServicos,
        BigDecimal totalItens,
        BigDecimal totalGeral,
        LocalDateTime criadoEm,
        LocalDateTime disponibilizadoEm
) {
    public static OrcamentoPublicoResponse from(OrcamentoEntity orcamentoEntity) {
        return new OrcamentoPublicoResponse(orcamentoEntity.getId(),
                orcamentoEntity.getOrdemServicoId(),
                orcamentoEntity.getNumeroOs(),
                orcamentoEntity.getTipo(),
                orcamentoEntity.getVersao(),
                orcamentoEntity.getStatus(),
                orcamentoEntity.getTotalServicos(),
                orcamentoEntity.getTotalItens(),
                orcamentoEntity.getTotalGeral(),
                orcamentoEntity.getCriadoEm(),
                orcamentoEntity.getDisponibilizadoEm());
    }
}
