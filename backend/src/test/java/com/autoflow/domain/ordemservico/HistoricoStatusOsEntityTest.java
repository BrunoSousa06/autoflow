package com.autoflow.domain.ordemservico;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HistoricoStatusOsTest {

    private HistoricoStatusOs historico;

    @BeforeEach
    void setUp() {
        historico = HistoricoStatusOs.criar(
                100L,
                StatusOrdemServico.RECEBIDA,
                "Ordem de serviço recebida com sucesso",
                "OS-001"
        );
    }

    @Test
    void testHistoricoCreation() {
        assertNotNull(historico);
        assertEquals(100L, historico.getOrdemServicoId());
        assertEquals(StatusOrdemServico.RECEBIDA, historico.getStatus());
        assertEquals("OS-001", historico.getNumeroOs());
        assertEquals("Ordem de serviço recebida com sucesso", historico.getMensagemCliente());
        assertNotNull(historico.getRegistradoEm());
    }

    @Test
    void testHistoricoSetters() {
        historico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        historico.setMensagemCliente("Iniciando diagnóstico");
        historico.setRegistradoEm(LocalDateTime.now());

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, historico.getStatus());
        assertEquals("Iniciando diagnóstico", historico.getMensagemCliente());
        assertNotNull(historico.getRegistradoEm());
    }

    @Test
    void testHistoricoMultiploStatus() {
        HistoricoStatusOs h1 = HistoricoStatusOs.criar(
                100L,
                StatusOrdemServico.RECEBIDA,
                "Recebida",
                "OS-001"
        );

        HistoricoStatusOs h2 = HistoricoStatusOs.criar(
                100L,
                StatusOrdemServico.EM_DIAGNOSTICO,
                "Em diagnóstico",
                "OS-001"
        );

        HistoricoStatusOs h3 = HistoricoStatusOs.criar(
                100L,
                StatusOrdemServico.FINALIZADA,
                "Finalizada",
                "OS-001"
        );

        assertEquals(StatusOrdemServico.RECEBIDA, h1.getStatus());
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, h2.getStatus());
        assertEquals(StatusOrdemServico.FINALIZADA, h3.getStatus());
    }

    @Test
    void testHistoricoOrderServiceIdNotNull() {
        assertNotNull(historico.getOrdemServicoId());
        assertEquals(100L, historico.getOrdemServicoId());
    }

    @Test
    void testHistoricoStatusNotNull() {
        assertNotNull(historico.getStatus());
    }

    @Test
    void testHistoricoMensagemCliente() {
        String mensagem = "Veículo será entregue amanhã";
        historico.setMensagemCliente(mensagem);

        assertEquals(mensagem, historico.getMensagemCliente());
    }

    @Test
    void testHistoricoConstructor() {
        HistoricoStatusOs h = new HistoricoStatusOs();
        assertNull(h.getId());
        assertNull(h.getOrdemServicoId());
        assertNull(h.getStatus());
    }

    @Test
    void testHistoricoTimestamp() {
        LocalDateTime agora = LocalDateTime.now();
        historico.setRegistradoEm(agora);

        assertEquals(agora, historico.getRegistradoEm());
    }
}
