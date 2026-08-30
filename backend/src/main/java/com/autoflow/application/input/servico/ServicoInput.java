package com.autoflow.application.input.servico;

import java.math.BigDecimal;

public record ServicoInput(
        String nome,
        String descricao,
        BigDecimal valor
) {
}
