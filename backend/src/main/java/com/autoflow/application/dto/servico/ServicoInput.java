package com.autoflow.application.dto.servico;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record ServicoInput(
        String nome,
        String descricao,
        BigDecimal valor
) {
}
