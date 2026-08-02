package com.autoflow.application.gateway;

import com.autoflow.application.dto.notificacao.MensagemNotificacao;

public interface NotificacaoService {
    void enviar(MensagemNotificacao mensagem);
}
