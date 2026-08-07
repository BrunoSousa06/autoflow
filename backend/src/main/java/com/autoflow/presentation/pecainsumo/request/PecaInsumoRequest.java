package com.autoflow.presentation.pecainsumo.request;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record PecaInsumoRequest(
        @NotNull(message = "O nome da peça/insumo é obrigatório") String nome,
        BigDecimal valor,
        @PositiveOrZero(message = "A quantidade não pode ser negativa") int quantidade,
        @NotNull(message = "A categoria da peca/insumo e obrigatoria") CategoriaPecaInsumo tipo
) {
}
