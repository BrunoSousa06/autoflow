package com.autoflow.application.output.ordemservico;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServico;

public record OrdemServicoDetalheOutput(
        OrdemServico ordemServico,
        OrcamentoEntity orcamentoAtual
) {
}
