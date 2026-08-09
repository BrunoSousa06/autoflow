package com.autoflow.application.gateway;

import com.autoflow.application.dto.notificacao.OrcamentoNotificacao;

public interface OrcamentoNotificacaoGateway {

    void notificar(OrcamentoNotificacao notificacao);
}
