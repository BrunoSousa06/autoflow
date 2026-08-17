package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface ConsultarOrcamentoDaOsUseCase {
    OrcamentoEntity execute(Long id, String numeroOs);
}
