package com.autoflow.controller.ordemservico.acompanhamento.response;

import com.autoflow.controller.ordemservico.response.ServicoOsResponse;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;

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
            OrdemServicoEntity os,
            OrcamentoEntity orcamentoAtual,
            List<HistoricoStatusOsEntity> historico
    ) {
        return new AcompanhamentoOrdemServicoResponse(
                os.getNumeroOs(),
                os.getVeiculo().getPlaca(),
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
