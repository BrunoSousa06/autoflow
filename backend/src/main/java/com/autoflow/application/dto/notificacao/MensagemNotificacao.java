package com.autoflow.application.dto.notificacao;

public record MensagemNotificacao(
        String destinatario,
        String assunto,
        String corpo
) {
}
