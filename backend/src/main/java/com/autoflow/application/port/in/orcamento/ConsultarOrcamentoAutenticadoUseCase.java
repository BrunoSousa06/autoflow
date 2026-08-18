package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.Orcamento;

public interface ConsultarOrcamentoAutenticadoUseCase {
    Orcamento execute(Long id, String email);
}
