package com.autoflow.application.gateway;

import com.autoflow.application.input.notificacao.MensagemNotificacao;

public interface NotificacaoGateway {
    void enviar(MensagemNotificacao mensagem);
}
