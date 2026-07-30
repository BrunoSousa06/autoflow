package com.autoflow.application.dto.ordemservico.acompanhamento;

import com.autoflow.controller.ordemservico.response.ServicoOsResponse;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.presentation.ordemservico.acompanhamento.response.HistoricoStatusOsResponse;
import com.autoflow.presentation.ordemservico.acompanhamento.response.OrcamentoResumoResponse;

import java.time.LocalDateTime;
import java.util.List;

public record AcompanhamentoOrdemServicoOutput(
        String numeroOs,
        String placa,
        StatusOrdemServico statusAtual,
        LocalDateTime dataAbertura,
        LocalDateTime ultimaAtualizacao,
        List<ServicoOsResponse> servicosSolicitados,
        OrcamentoResumoResponse orcamentoAtual,
        StatusOrcamento situacaoAprovacao,
        String mensagemParaCliente,
        List<HistoricoStatusOsResponse> historicoStatus

) {
}