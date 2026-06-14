package com.autoflow.controller.pecainsumo.response;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public record PecaInsumoResponse(
        Long id,
        String nome,
        BigDecimal valor,
        int quantidade,
        CategoriaPecaInsumo tipo) {
}
