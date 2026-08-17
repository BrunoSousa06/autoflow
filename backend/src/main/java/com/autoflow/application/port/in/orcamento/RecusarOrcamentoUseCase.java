package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface RecusarOrcamentoUseCase {
    OrcamentoEntity execute(OrcamentoEntity orcamento, String motivo, String assinaturaNome);
}
