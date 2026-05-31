package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface PublicOrcamentoService {
    OrcamentoEntity consultar(Long orcamentoId, String token);
    OrcamentoEntity aceitar(Long orcamentoId, String token, String nome);
    OrcamentoEntity recusar(Long orcamentoId, String token, String motivo);
}