package com.autoflow.presentation.ordemservico.acompanhamento.response;

import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.presentation.ordemservico.response.ServicoOsResponse;

import java.time.LocalDateTime;
import java.util.List;

public record AcompanhamentoOrdemServicoResponse(
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
    public static AcompanhamentoOrdemServicoResponse from(
            OrdemServico os,
            Orcamento orcamentoAtual,
            List<HistoricoStatusOs> historico
    ) {
        return new AcompanhamentoOrdemServicoResponse(
                os.getNumeroOs(),
                os.getVeiculo().placa(),
                os.getStatus(),
                os.getDataAbertura(),
                os.getUltimaAtualizacao(),
                os.getServicosSolicitados().stream()
                        .map(ServicoOsResponse::fromDomain)
                        .toList(),
                orcamentoAtual == null ? null : OrcamentoResumoResponse.from(orcamentoAtual),
                orcamentoAtual == null ? null : orcamentoAtual.getStatus(),
                mensagemParaCliente(os.getStatus()),
                historico.stream()
                        .map(HistoricoStatusOsResponse::from)
                        .toList()
        );
    }

    public static String mensagemParaCliente(StatusOrdemServico status) {
        return switch (status) {
            case RECEBIDA -> "Recebemos sua ordem de serviço. Em breve iniciaremos o diagnóstico.";
            case EM_DIAGNOSTICO -> "Seu veículo está em diagnóstico técnico.";
            case AGUARDANDO_APROVACAO -> "O orçamento está disponível e aguardando sua aprovação.";
            case EM_EXECUCAO -> "Os serviços aprovados estão em execução.";
            case FINALIZADA -> "Os serviços foram finalizados. Seu veículo está aguardando entrega.";
            case ENTREGUE -> "Seu veículo foi entregue. Obrigado por utilizar a AutoFlow.";
        };
    }
}
