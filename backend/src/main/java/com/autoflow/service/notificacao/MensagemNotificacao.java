package com.autoflow.service.notificacao;

public record MensagemNotificacao(
        String destinatario,
        String assunto,
        String corpo
) {
}