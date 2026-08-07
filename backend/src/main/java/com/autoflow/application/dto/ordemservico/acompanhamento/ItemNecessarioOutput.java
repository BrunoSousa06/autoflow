package com.autoflow.application.dto.ordemservico.acompanhamento;

import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public record ItemNecessarioOutput(
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
    public static ItemNecessarioOutput from(ItemNecessarioEntity item) {
        return new ItemNecessarioOutput(
                item.getPecaInsumoId(), item.getNome(), item.getTipo(), item.getValorUnitario(),
                item.getQuantidade(), item.getValorTotal(), item.getStatus(), item.getMotivoPendencia(),
                item.getQuantidadeDisponivel(), item.getMensagemStatus());
    }
}
