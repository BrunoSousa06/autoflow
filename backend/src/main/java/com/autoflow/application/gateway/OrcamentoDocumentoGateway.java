package com.autoflow.application.gateway;

import com.autoflow.domain.orcamento.Orcamento;

public interface OrcamentoDocumentoGateway {

    byte[] gerarPdf(Orcamento orcamento);
}
