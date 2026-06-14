package com.autoflow.controller.ordemservico.response;

import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public record ItemNecessarioResponse(
        Long pecaInsumoId,
        String nome,
        CategoriaPecaInsumo tipo,
        BigDecimal valorUnitario,
        Integer quantidade,
        BigDecimal valorTotal,
        StatusItemNecessario status,
        MotivoPendenciaItem motivoPendencia,
        Integer quantidadeDisponivel,
        String mensagemStatus
) {
    public static ItemNecessarioResponse fromDomain(ItemNecessarioEntity item) {
        return new ItemNecessarioResponse(
                item.getPecaInsumoId(),
                item.getNome(),
                item.getTipo(),
                item.getValorUnitario(),
                item.getQuantidade(),
                item.getValorTotal(),
                item.getStatus(),
                item.getMotivoPendencia(),
                item.getQuantidadeDisponivel(),
                item.getMensagemStatus()
        );
    }
}