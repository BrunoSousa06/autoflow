package com.autoflow.infrastructure.notificacao;

import com.autoflow.application.dto.notificacao.MensagemNotificacao;
import com.autoflow.application.gateway.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificacaoService implements NotificacaoService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    @Override
    public void enviar(MensagemNotificacao mensagem) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(remetente);
        email.setTo(mensagem.destinatario());
        email.setSubject(mensagem.assunto());
        email.setText(mensagem.corpo());
        mailSender.send(email);
    }
}
