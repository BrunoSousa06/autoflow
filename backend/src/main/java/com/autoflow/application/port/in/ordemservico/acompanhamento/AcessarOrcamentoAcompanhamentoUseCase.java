package com.autoflow.application.port.in.ordemservico.acompanhamento;

import com.autoflow.domain.orcamento.Orcamento;

public interface AcessarOrcamentoAcompanhamentoUseCase {
    Orcamento consultar(Long orcamentoId, String token);
    Orcamento aprovar(Long orcamentoId, String token);
}
