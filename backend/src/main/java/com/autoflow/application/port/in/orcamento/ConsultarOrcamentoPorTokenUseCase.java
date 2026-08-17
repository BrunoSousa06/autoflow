package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface ConsultarOrcamentoPorTokenUseCase {
    OrcamentoEntity execute(Long id, String token);
}
