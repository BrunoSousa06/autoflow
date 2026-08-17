package com.autoflow.application.port.in.ordemservico.acompanhamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface AcessarOrcamentoAcompanhamentoUseCase {
    OrcamentoEntity consultar(Long orcamentoId, String token);
    OrcamentoEntity aprovar(Long orcamentoId, String token);
}
