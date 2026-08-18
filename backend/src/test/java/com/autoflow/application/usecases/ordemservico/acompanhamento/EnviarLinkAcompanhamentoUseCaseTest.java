package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.input.notificacao.MensagemNotificacao;
import com.autoflow.application.gateway.NotificacaoGateway;
import com.autoflow.application.usecases.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCaseImpl;
import com.autoflow.domain.ordemservico.ClienteOs;
import com.autoflow.domain.ordemservico.OrdemServico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnviarLinkAcompanhamentoUseCaseTest {

    @Mock
    private NotificacaoGateway notificacaoGateway;

    private EnviarLinkAcompanhamentoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new EnviarLinkAcompanhamentoUseCaseImpl(notificacaoGateway, "http://localhost:4200");
    }

    @Test
    void deveEnviarLinkEAlternativaDeLoginParaCliente() {
        ClienteOs cliente = mock(ClienteOs.class);
        when(cliente.getNome()).thenReturn("Maria");
        when(cliente.getEmail()).thenReturn("maria@email.com");
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setCliente(cliente);
        ordemServico.setNumeroOs("OS-123");

        useCase.execute(ordemServico, "token-seguro");

        ArgumentCaptor<MensagemNotificacao> captor = ArgumentCaptor.forClass(MensagemNotificacao.class);
        verify(notificacaoGateway).enviar(captor.capture());
        MensagemNotificacao mensagem = captor.getValue();
        assertAll(
                () -> assertEquals("maria@email.com", mensagem.destinatario()),
                () -> assertTrue(mensagem.corpo().contains("http://localhost:4200/public/acompanhamento?token=token-seguro")),
                () -> assertTrue(mensagem.corpo().contains("login e senha")),
                () -> assertTrue(mensagem.corpo().contains("Minha Conta"))
        );
    }

    @Test
    void naoDeveEnviarQuandoClienteNaoPossuiEmail() {
        ClienteOs cliente = mock(ClienteOs.class);
        when(cliente.getEmail()).thenReturn(" ");
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setCliente(cliente);

        useCase.execute(ordemServico, "token");

        verify(notificacaoGateway, never()).enviar(org.mockito.ArgumentMatchers.any());
    }
}
