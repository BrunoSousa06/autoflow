package com.autoflow.presentation.orcamento.response;

import com.autoflow.domain.orcamento.OrcamentoItemNecessarioEntity;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public record OrcamentoItemNecessarioResponse(
        Long pecaInsumoId,
        Long servicoOsId,
        String nome,
        CategoriaPecaInsumo tipo,
        BigDecimal valorUnitario,
        Integer quantidade,
        BigDecimal valorTotal
) {
    public static OrcamentoItemNecessarioResponse from(OrcamentoItemNecessarioEntity item) {
        return new OrcamentoItemNecessarioResponse(
                item.getPecaInsumoId(),
                item.getServicoOsId(),
                item.getNome(),
                item.getTipo(),
                item.getValorUnitario(),
                item.getQuantidade(),
                item.getValorTotal()
        );
    }
}
