package com.autoflow.service.notificacao;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificacaoService implements NotificacaoService{

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    @Override
    public void enviar(MensagemNotificacao message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(remetente);
        email.setTo(message.destinatario());
        email.setSubject(message.assunto());
        email.setText(message.corpo());

        mailSender.send(email);

    }
}
