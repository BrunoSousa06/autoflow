package com.autoflow.application.output.pecainsumo;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public record PecaInsumoOutput(
        Long id,
        String nome,
        BigDecimal valor,
        int quantidade,
        CategoriaPecaInsumo tipo) {
}
