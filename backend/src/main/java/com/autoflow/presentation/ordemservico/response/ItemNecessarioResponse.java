package com.autoflow.presentation.ordemservico.response;

import com.autoflow.domain.ordemservico.ItemNecessario;
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
    public static ItemNecessarioResponse fromDomain(ItemNecessario item) {
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
