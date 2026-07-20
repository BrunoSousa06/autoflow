package com.autoflow.application.dto.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record ServicoInput(
        @NotBlank(message = "O nome não pode estar em branco")
        String nome,

        @NotBlank(message = "A descrição não pode estar em branco")
        String descricao,

        @DecimalMin(value = "0.0", inclusive = false, message = "O valor deve ser maior que zero")
        BigDecimal valor
) {
}
