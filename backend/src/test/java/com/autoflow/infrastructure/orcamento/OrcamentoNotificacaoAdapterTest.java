package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.dto.notificacao.MensagemNotificacao;
import com.autoflow.application.dto.notificacao.OrcamentoNotificacao;
import com.autoflow.application.gateway.NotificacaoGateway;
import com.autoflow.domain.orcamento.TipoOrcamento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrcamentoNotificacaoAdapterTest {

    @Mock
    private NotificacaoGateway notificacaoGateway;

    @InjectMocks
    private OrcamentoNotificacaoAdapter adapter;

    @Test
    void deveEnviarLinkDeDecisaoNaNotificacao() {
        adapter.notificar(new OrcamentoNotificacao(
                10L,
                TipoOrcamento.PRINCIPAL,
                "OS-10",
                "Maria",
                "maria@exemplo.com",
                "https://api.test/public/orcamentos/10/pdf?token=abc",
                "https://app.test/public/orcamentos/10?token=abc"
        ));

        ArgumentCaptor<MensagemNotificacao> captor = ArgumentCaptor.forClass(MensagemNotificacao.class);
        verify(notificacaoGateway).enviar(captor.capture());
        assertTrue(captor.getValue().corpo().contains("https://app.test/public/orcamentos/10?token=abc"));
    }
}
