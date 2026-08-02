package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.dto.notificacao.MensagemNotificacao;
import com.autoflow.application.gateway.NotificacaoService;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ClienteOsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EnviarLinkAcompanhamentoUseCaseTest {

    @Mock
    private NotificacaoService notificacaoService;

    private EnviarLinkAcompanhamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EnviarLinkAcompanhamentoUseCase(notificacaoService);
        ReflectionTestUtils.setField(useCase, "frontendPublicBaseUrl", "https://app.autoflow.com");
    }

    @Test
    void deveEnviarLinkEAlternativaDeLoginParaCliente() {
        ClienteOsEntity cliente = mock(ClienteOsEntity.class);
        when(cliente.getNome()).thenReturn("Maria");
        when(cliente.getEmail()).thenReturn("maria@email.com");
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setCliente(cliente);
        ordemServico.setNumeroOs("OS-123");

        useCase.execute(ordemServico, "token-seguro");

        ArgumentCaptor<MensagemNotificacao> captor = ArgumentCaptor.forClass(MensagemNotificacao.class);
        verify(notificacaoService).enviar(captor.capture());
        MensagemNotificacao mensagem = captor.getValue();
        assertAll(
                () -> assertEquals("maria@email.com", mensagem.destinatario()),
                () -> assertTrue(mensagem.corpo().contains("https://app.autoflow.com/public/acompanhamento?token=token-seguro")),
                () -> assertTrue(mensagem.corpo().contains("login e senha")),
                () -> assertTrue(mensagem.corpo().contains("Minha Conta"))
        );
    }

    @Test
    void naoDeveEnviarQuandoClienteNaoPossuiEmail() {
        ClienteOsEntity cliente = mock(ClienteOsEntity.class);
        when(cliente.getEmail()).thenReturn(" ");
        OrdemServicoEntity ordemServico = new OrdemServicoEntity();
        ordemServico.setCliente(cliente);

        useCase.execute(ordemServico, "token");

        verify(notificacaoService, never()).enviar(org.mockito.ArgumentMatchers.any());
    }
}
