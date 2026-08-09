package com.autoflow.application.dto.ordemservico;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;

public record OrdemServicoDetalheOutput(
        OrdemServicoEntity ordemServico,
        OrcamentoEntity orcamentoAtual
) {
}
