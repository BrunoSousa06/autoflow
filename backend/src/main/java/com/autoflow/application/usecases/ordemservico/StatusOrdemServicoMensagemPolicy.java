package com.autoflow.application.usecases.ordemservico;

import com.autoflow.domain.ordemservico.StatusOrdemServico;

public final class StatusOrdemServicoMensagemPolicy {
    private StatusOrdemServicoMensagemPolicy() {}

    public static String mensagem(StatusOrdemServico status) {
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
