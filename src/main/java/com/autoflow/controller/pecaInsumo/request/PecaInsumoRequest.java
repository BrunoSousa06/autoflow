package com.autoflow.controller.pecaInsumo.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PecaInsumoRequest(
        @NotNull(message = "O nome da peça/insumo é obrigatório") String nome,
        BigDecimal valor,
        int quantidade
) {
}
