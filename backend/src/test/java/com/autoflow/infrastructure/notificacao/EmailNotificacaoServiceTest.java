package com.autoflow.infrastructure.notificacao;

import com.autoflow.application.dto.notificacao.MensagemNotificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificacaoServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificacaoService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "remetente", "noreply@autoflow.com");
    }

    @Test
    void deveEnviarEmailComDadosCorretos() {
        MensagemNotificacao mensagem = new MensagemNotificacao(
                "cliente@email.com", "Orçamento disponível", "Seu orçamento foi gerado."
        );

        service.enviar(mensagem);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage emailEnviado = captor.getValue();
        assertEquals("noreply@autoflow.com", emailEnviado.getFrom());
        assertArrayEquals(new String[]{"cliente@email.com"}, emailEnviado.getTo());
        assertEquals("Orçamento disponível", emailEnviado.getSubject());
        assertEquals("Seu orçamento foi gerado.", emailEnviado.getText());
    }

    @Test
    void deveChamarMailSenderUmaVez() {
        MensagemNotificacao mensagem = new MensagemNotificacao(
                "destino@email.com", "Assunto", "Corpo"
        );

        service.enviar(mensagem);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
