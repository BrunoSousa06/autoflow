package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface AprovarOrcamentoUseCase {
    OrcamentoEntity execute(OrcamentoEntity orcamento, String assinaturaNome);
}
