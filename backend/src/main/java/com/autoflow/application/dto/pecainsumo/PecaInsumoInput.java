package com.autoflow.application.dto.pecainsumo;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public record PecaInsumoInput(
        String nome,
        BigDecimal valor,
        int quantidade,
        CategoriaPecaInsumo tipo
) {
}
