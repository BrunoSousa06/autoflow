package com.autoflow.presentation.orcamento.response;

import com.autoflow.domain.orcamento.OrcamentoServico;

import java.math.BigDecimal;

public record OrcamentoServicoResponse(
        Long servicoId,
        String nome,
        BigDecimal valor
) {
    public static OrcamentoServicoResponse from(OrcamentoServico servico) {
        return new OrcamentoServicoResponse(
                servico.getServicoId(),
                servico.getNome(),
                servico.getValor()
        );
    }
}
