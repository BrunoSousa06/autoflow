package com.autoflow.controller.orcamento.response;

import com.autoflow.domain.orcamento.OrcamentoServicoEntity;

import java.math.BigDecimal;

public record OrcamentoServicoResponse(
        Long servicoId,
        String nome,
        BigDecimal valor
) {
    public static OrcamentoServicoResponse from(OrcamentoServicoEntity servico) {
        return new OrcamentoServicoResponse(
                servico.getServicoId(),
                servico.getNome(),
                servico.getValor()
        );
    }
}
