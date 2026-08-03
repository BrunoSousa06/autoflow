package com.autoflow.application.gateway;

import com.autoflow.application.dto.notificacao.MensagemNotificacao;

public interface NotificacaoGateway {
    void enviar(MensagemNotificacao mensagem);
}
