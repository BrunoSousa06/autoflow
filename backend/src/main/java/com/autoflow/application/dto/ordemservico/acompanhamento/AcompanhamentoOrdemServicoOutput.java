package com.autoflow.application.dto.ordemservico.acompanhamento;

import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;
import java.util.List;

public record AcompanhamentoOrdemServicoOutput(
        String numeroOs,
        String placa,
        StatusOrdemServico statusAtual,
        LocalDateTime dataAbertura,
        LocalDateTime ultimaAtualizacao,
        List<ServicoSolicitadoOutput> servicosSolicitados,
        OrcamentoResumoOutput orcamentoAtual,
        StatusOrcamento situacaoAprovacao,
        String mensagemParaCliente,
        List<HistoricoStatusOsOutput> historicoStatus

) {
}
