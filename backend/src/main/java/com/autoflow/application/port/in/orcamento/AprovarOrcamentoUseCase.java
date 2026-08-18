package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.Orcamento;

public interface AprovarOrcamentoUseCase {
    Orcamento execute(Orcamento orcamento, String assinaturaNome);
}
