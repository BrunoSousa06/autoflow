package com.autoflow.controller.pecaInsumo.response;

import java.math.BigDecimal;

public record PecaInsumoResponse(
        Long id,
        String nome,
        BigDecimal valor,
        int quantidade) {
}
