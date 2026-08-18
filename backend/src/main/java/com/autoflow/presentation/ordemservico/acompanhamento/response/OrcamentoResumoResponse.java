package com.autoflow.presentation.ordemservico.acompanhamento.response;


import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrcamentoResumoResponse(
        Long id,
        TipoOrcamento tipo,
        Integer versao,
        StatusOrcamento status,
        BigDecimal totalServicos,
        BigDecimal totalItens,
        BigDecimal totalGeral,
        LocalDateTime criadoEm,
        LocalDateTime disponibilizadoEm,
        LocalDateTime aprovadoEm,
        LocalDateTime reprovadoEm,
        String mensagem
) {
    public static OrcamentoResumoResponse from(Orcamento orcamento) {
        return new OrcamentoResumoResponse(
                orcamento.getId(),
                orcamento.getTipo(),
                orcamento.getVersao(),
                orcamento.getStatus(),
                orcamento.getTotalServicos(),
                orcamento.getTotalItens(),
                orcamento.getTotalGeral(),
                orcamento.getCriadoEm(),
                orcamento.getDisponibilizadoEm(),
                orcamento.getAprovadoEm(),
                orcamento.getReprovadoEm(),
                mensagem(orcamento.getStatus())
        );
    }

    private static String mensagem(StatusOrcamento status) {
        return switch (status) {
            case DISPONIVEL -> "Orçamento disponível para aprovação.";
            case APROVADO -> "Orçamento aprovado.";
            case REPROVADO -> "Orçamento recusado.";
            case SUBSTITUIDO -> "Este orçamento foi substituído por uma versão mais recente.";
        };
    }
}