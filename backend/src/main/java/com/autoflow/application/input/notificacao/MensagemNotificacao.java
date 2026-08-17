package com.autoflow.application.input.notificacao;

public record MensagemNotificacao(
        String destinatario,
        String assunto,
        String corpo
) {
}
