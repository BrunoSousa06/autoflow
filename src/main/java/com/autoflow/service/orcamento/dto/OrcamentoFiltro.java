package com.autoflow.service.orcamento.dto;

import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;

public record OrcamentoFiltro(
        StatusOrcamento status,
        String numeroOs,
        String placa,
        String clienteEmail,
        String clienteDocumento,
        TipoOrcamento tipo
) {
}
