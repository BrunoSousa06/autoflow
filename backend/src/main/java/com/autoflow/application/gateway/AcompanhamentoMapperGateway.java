package com.autoflow.application.gateway;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;

import java.util.List;

public interface AcompanhamentoMapperGateway {

    AcompanhamentoOrdemServicoOutput mapToOutput(
            OrdemServicoEntity ordemServico,
            OrcamentoEntity orcamento,
            List<HistoricoStatusOsEntity> historico);
}
