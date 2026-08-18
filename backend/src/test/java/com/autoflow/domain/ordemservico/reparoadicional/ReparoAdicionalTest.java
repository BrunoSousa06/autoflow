package com.autoflow.domain.ordemservico.reparoadicional;

import com.autoflow.domain.ordemservico.ServicoSolicitado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReparoAdicionalTest {

    private ReparoAdicional reparoAdicional;
    private List<ServicoSolicitado> servicos;

    @BeforeEach
    void setUp() {
        servicos = new ArrayList<>();
        reparoAdicional = ReparoAdicional.criar("OS-001", 1L, servicos);
    }

    @Test
    void testReparoAdicionalCreation() {
        assertNotNull(reparoAdicional);
        assertEquals("OS-001", reparoAdicional.getNumeroOs());
        assertEquals(1L, reparoAdicional.getMecanicoId());
        assertEquals(StatusReparoAdicional.PENDENTE_APROVACAO, reparoAdicional.getStatus());
        assertNotNull(reparoAdicional.getCriadoEm());
    }

    @Test
    void testReparoAdicionalSetters() {
        reparoAdicional.setOrcamentoId(100L);
        reparoAdicional.setOrdemServicoId(50L);

        assertEquals(100L, reparoAdicional.getOrcamentoId());
        assertEquals(50L, reparoAdicional.getOrdemServicoId());
    }

    @Test
    void testReparoAdicionalAprovar() {
        reparoAdicional.aprovar();

        assertEquals(StatusReparoAdicional.APROVADO, reparoAdicional.getStatus());
        assertNotNull(reparoAdicional.getAprovadoEm());
    }

    @Test
    void testReparoAdicionalAprovarJaAprovado() {
        reparoAdicional.aprovar();

        assertThrows(IllegalStateException.class, () -> {
            reparoAdicional.aprovar();
        });
    }

    @Test
    void testReparoAdicionalRecusar() {
        String motivo = "Serviço não autorizado";
        reparoAdicional.recusar(motivo);

        assertEquals(StatusReparoAdicional.RECUSADO, reparoAdicional.getStatus());
        assertNotNull(reparoAdicional.getRecusadoEm());
        assertEquals(motivo, reparoAdicional.getMotivoRecusa());
    }

    @Test
    void testReparoAdicionalRecusarJaRecusado() {
        reparoAdicional.recusar("Motivo 1");

        assertThrows(IllegalStateException.class, () -> {
            reparoAdicional.recusar("Motivo 2");
        });
    }

    @Test
    void testReparoAdicionalServicosCollection() {
        assertNotNull(reparoAdicional.getServicos());
        assertTrue(reparoAdicional.getServicos().isEmpty());
    }

    @Test
    void testReparoAdicionalDefaultStatus() {
        ReparoAdicional novoReparo = new ReparoAdicional();
        assertEquals(StatusReparoAdicional.PENDENTE_APROVACAO, novoReparo.getStatus());
    }

    @Test
    void testReparoAdicionalTransitionPendingToAprovado() {
        assertEquals(StatusReparoAdicional.PENDENTE_APROVACAO, reparoAdicional.getStatus());
        reparoAdicional.aprovar();
        assertEquals(StatusReparoAdicional.APROVADO, reparoAdicional.getStatus());
    }

    @Test
    void testReparoAdicionalTransitionPendingToRecusado() {
        assertEquals(StatusReparoAdicional.PENDENTE_APROVACAO, reparoAdicional.getStatus());
        reparoAdicional.recusar("Motivo");
        assertEquals(StatusReparoAdicional.RECUSADO, reparoAdicional.getStatus());
    }
}
