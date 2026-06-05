package com.autoflow.controller.ordemservico.response;

import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
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
        StatusItemNecessario status
) {
    public static ItemNecessarioResponse fromDomain(ItemNecessarioEntity itensNecessarios) {
        return new ItemNecessarioResponse(itensNecessarios.getPecaInsumoId(),
                itensNecessarios.getNome(),
                itensNecessarios.getTipo(),
                itensNecessarios.getValorUnitario(),
                itensNecessarios.getQuantidade(),
                itensNecessarios.getValorTotal(),
                itensNecessarios.getStatus());
    }
}