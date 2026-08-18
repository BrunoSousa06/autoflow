package com.autoflow.application.output.ordemservico;

import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.ordemservico.OrdemServico;

public record OrdemServicoDetalheOutput(
        OrdemServico ordemServico,
        Orcamento orcamentoAtual
) {
}
