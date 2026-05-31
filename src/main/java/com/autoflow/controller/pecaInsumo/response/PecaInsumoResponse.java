package com.autoflow.controller.pecaInsumo.response;

import com.autoflow.domain.pecaInsumo.CategoriaPecaInsumo;

import java.math.BigDecimal;

public record PecaInsumoResponse(
        Long id,
        String nome,
        BigDecimal valor,
        int quantidade,
        CategoriaPecaInsumo tipo) {
}
