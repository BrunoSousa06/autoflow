package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.OrcamentoEntity;

public interface OrcamentoDocumentoGateway {

    byte[] gerarPdf(OrcamentoEntity orcamento);
}
