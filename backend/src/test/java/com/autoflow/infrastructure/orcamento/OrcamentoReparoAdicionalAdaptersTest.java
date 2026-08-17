package com.autoflow.infrastructure.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.application.gateway.NotificacaoGateway;
import com.autoflow.application.input.notificacao.MensagemNotificacao;
import com.autoflow.application.input.notificacao.OrcamentoNotificacao;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoReparoAdicionalAdaptersTest {

    @Mock OrcamentoVersioningGateway versioningGateway;
    @Mock OrcamentoFactory factory;
    @Mock OrcamentoGateway orcamentoGateway;
    @Mock NotificacaoGateway notificacaoGateway;

    @Test
    void deveComporVersaoFactoryEPersistenciaDoOrcamentoComplementar() {
        var adapter = new OrcamentoComplementarAdapter(
                versioningGateway,
                factory,
                orcamentoGateway
        );
        var ordemServico = new OrdemServico();
        ordemServico.setId(10L);
        ordemServico.setNumeroOs("OS-123");
        var reparo = new ReparoAdicional();
        var criadoEm = LocalDateTime.of(2026, 8, 2, 12, 30);
        var orcamentoCriado = new OrcamentoEntity();
        var orcamentoSalvo = new OrcamentoEntity();

        when(versioningGateway.proximaVersao(10L, TipoOrcamento.COMPLEMENTAR)).thenReturn(2);
        when(factory.criarAdicionalDisponivel(ordemServico, reparo, 2, criadoEm))
                .thenReturn(orcamentoCriado);
        when(orcamentoGateway.save(orcamentoCriado)).thenReturn(orcamentoSalvo);

        var resultado = adapter.criarESalvar(ordemServico, reparo, criadoEm);

        assertSame(orcamentoSalvo, resultado);
        InOrder ordem = inOrder(versioningGateway, factory, orcamentoGateway);
        ordem.verify(versioningGateway).proximaVersao(10L, TipoOrcamento.COMPLEMENTAR);
        ordem.verify(factory).criarAdicionalDisponivel(ordemServico, reparo, 2, criadoEm);
        ordem.verify(orcamentoGateway).save(orcamentoCriado);
    }

    @Test
    void deveDelegarNotificacaoParaAbstracaoExistente() {
        var adapter = new OrcamentoNotificacaoAdapter(notificacaoGateway);
        var notificacao = new OrcamentoNotificacao(
                30L, TipoOrcamento.PRINCIPAL, "OS-123", "Cliente", "cliente@example.com",
                "https://publicacao/orcamento/30");

        adapter.notificar(notificacao);

        ArgumentCaptor<MensagemNotificacao> mensagemCaptor =
                ArgumentCaptor.forClass(MensagemNotificacao.class);
        verify(notificacaoGateway).enviar(mensagemCaptor.capture());
        assertEquals("cliente@example.com", mensagemCaptor.getValue().destinatario());
        assertEquals("Orçamento disponível - AutoFlow", mensagemCaptor.getValue().assunto());
        assertEquals("""
                Olá, Cliente.

                O orçamento #30 da sua ordem de serviço OS-123 está disponível.

                Para baixar o PDF do orçamento, acesse o link abaixo:

                https://publicacao/orcamento/30

                Atenciosamente,
                AutoFlow
                """, mensagemCaptor.getValue().corpo());
    }

    @Test
    void naoDeveEnviarNotificacaoQuandoClienteNaoTemEmail() {
        var adapter = new OrcamentoNotificacaoAdapter(notificacaoGateway);
        var notificacao = new OrcamentoNotificacao(
                30L, TipoOrcamento.PRINCIPAL, "OS-123", "Cliente", " ",
                "https://publicacao/orcamento/30");

        adapter.notificar(notificacao);

        verifyNoInteractions(notificacaoGateway);
    }

    @Test
    void deveComporNotificacaoComplementarNoAdapterAtivo() {
        var adapter = new OrcamentoNotificacaoAdapter(notificacaoGateway);
        var notificacao = new OrcamentoNotificacao(
                30L, TipoOrcamento.COMPLEMENTAR, "OS-123", "Cliente", "cliente@example.com",
                "https://publicacao/orcamento/30");

        adapter.notificar(notificacao);

        ArgumentCaptor<MensagemNotificacao> mensagemCaptor =
                ArgumentCaptor.forClass(MensagemNotificacao.class);
        verify(notificacaoGateway).enviar(mensagemCaptor.capture());
        assertEquals("Orçamento complementar aguardando aprovação - AutoFlow", mensagemCaptor.getValue().assunto());
        assertEquals("""
                Olá, Cliente.

                Durante a execução da ordem de serviço OS-123, identificamos a necessidade de um orçamento complementar.

                O orçamento complementar #30 está disponível para sua análise e aprovação.

                Para baixar o PDF do orçamento complementar, acesse o link abaixo:

                https://publicacao/orcamento/30

                Importante: este orçamento é complementar ao orçamento principal já aprovado.

                Atenciosamente,
                AutoFlow
                """, mensagemCaptor.getValue().corpo());
    }
}
