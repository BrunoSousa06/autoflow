package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface OrcamentoPdfService {

    byte[] gerarPdf(OrcamentoEntity orcamento);
}