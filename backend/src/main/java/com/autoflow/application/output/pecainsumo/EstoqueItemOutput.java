package com.autoflow.application.output.pecainsumo;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public record EstoqueItemOutput(
        Long id,
        String nome,
        CategoriaPecaInsumo tipo,
        BigDecimal valor,
        int quantidade
) {
}
