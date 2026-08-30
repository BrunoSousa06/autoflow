package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.Orcamento;

public interface ConsultarOrcamentoDaOsUseCase {
    Orcamento execute(Long id, String numeroOs);
}
