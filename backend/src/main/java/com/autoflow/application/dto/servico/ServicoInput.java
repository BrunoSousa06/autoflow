package com.autoflow.application.dto.servico;

import java.math.BigDecimal;

public record ServicoInput(
        String nome,
        String descricao,
        BigDecimal valor
) {
}
