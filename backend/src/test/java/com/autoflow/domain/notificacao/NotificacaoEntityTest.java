package com.autoflow.domain.notificacao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificacaoEntityTest {

    private NotificacaoEntity notificacao;

    @BeforeEach
    void setUp() {
        notificacao = new NotificacaoEntity();
        notificacao.setId(1L);
        notificacao.setOrcamentoId(100L);
        notificacao.setClienteId(50L);
        notificacao.setCanal(CanalNotificacao.EMAIL);
        notificacao.setDestinatario("cliente@example.com");
        notificacao.setStatus(StatusNotificacao.PENDENTE);
        notificacao.setCriadaEm(LocalDateTime.now());
    }

    @Test
    void testNotificacaoPendenteFactory() {
        NotificacaoEntity notif = NotificacaoEntity.pendente(100L, 50L, "cliente@example.com");

        assertNotNull(notif);
        assertEquals(100L, notif.getOrcamentoId());
        assertEquals(50L, notif.getClienteId());
        assertEquals(CanalNotificacao.EMAIL, notif.getCanal());
        assertEquals("cliente@example.com", notif.getDestinatario());
        assertEquals(StatusNotificacao.PENDENTE, notif.getStatus());
        assertNotNull(notif.getCriadaEm());
    }

    @Test
    void testMarcarComoEnviada() {
        notificacao.marcarComoEnviada();

        assertEquals(StatusNotificacao.ENVIADA, notificacao.getStatus());
        assertNotNull(notificacao.getEnviadaEm());
        assertNull(notificacao.getMensagemErro());
    }

    @Test
    void testMarcarComoFalha() {
        String mensagemErro = "SMTP connection failed";
        notificacao.marcarComoFalha(mensagemErro);

        assertEquals(StatusNotificacao.FALHA, notificacao.getStatus());
        assertEquals(mensagemErro, notificacao.getMensagemErro());
    }

    @Test
    void testNotificacaoSetters() {
        notificacao.setOrcamentoId(200L);
        notificacao.setClienteId(75L);
        notificacao.setCanal(CanalNotificacao.EMAIL);
        notificacao.setDestinatario("novo@example.com");
        notificacao.setStatus(StatusNotificacao.ENVIADA);

        assertEquals(200L, notificacao.getOrcamentoId());
        assertEquals(75L, notificacao.getClienteId());
        assertEquals(CanalNotificacao.EMAIL, notificacao.getCanal());
        assertEquals("novo@example.com", notificacao.getDestinatario());
        assertEquals(StatusNotificacao.ENVIADA, notificacao.getStatus());
    }

    @Test
    void testNotificacaoStatusTransition() {
        notificacao.setStatus(StatusNotificacao.PENDENTE);
        assertEquals(StatusNotificacao.PENDENTE, notificacao.getStatus());

        notificacao.marcarComoEnviada();
        assertEquals(StatusNotificacao.ENVIADA, notificacao.getStatus());
    }

    @Test
    void testNotificacaoMensagemErroNullable() {
        assertNull(notificacao.getMensagemErro());
        notificacao.setMensagemErro("Erro de conexão");
        assertEquals("Erro de conexão", notificacao.getMensagemErro());
    }

    @Test
    void testNotificacaoCanal() {
        for (CanalNotificacao canal : CanalNotificacao.values()) {
            notificacao.setCanal(canal);
            assertEquals(canal, notificacao.getCanal());
        }
    }
}
