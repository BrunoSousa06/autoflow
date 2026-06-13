package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;

import java.util.List;

public interface PublicOrcamentoService {
    OrcamentoEntity consultar(Long orcamentoId, String token);
    OrcamentoEntity aprovar(Long orcamentoId, String token, String nome);
    OrcamentoEntity recusar(Long orcamentoId, String token, String motivo);
    List<OrcamentoEntity> consultarOrcamentos(StatusOrcamento status);
    OrcamentoEntity consultarPdf(Long orcamentoId, String token);
}