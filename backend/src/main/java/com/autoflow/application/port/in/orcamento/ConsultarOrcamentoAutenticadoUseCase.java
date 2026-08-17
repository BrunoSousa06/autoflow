package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface ConsultarOrcamentoAutenticadoUseCase {
    OrcamentoEntity execute(Long id, String email);
}
