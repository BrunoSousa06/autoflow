package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.Orcamento;

public interface RecusarOrcamentoUseCase {
    Orcamento execute(Orcamento orcamento, String motivo, String assinaturaNome);
}
