package com.autoflow.application.port.in.orcamento;

import com.autoflow.domain.orcamento.Orcamento;

public interface ConsultarOrcamentoPorTokenUseCase {
    Orcamento execute(Long id, String token);
}
