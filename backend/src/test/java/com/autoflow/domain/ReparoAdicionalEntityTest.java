package com.autoflow.domain;

import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import com.autoflow.domain.ordemservico.reparoadicional.StatusReparoAdicional;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReparoAdicionalTest {

    @Test
    void deveCriarReparoAdicionalPendenteEVincularServicos() {
        ServicoSolicitado servico = new ServicoSolicitado(1L, "Troca de pastilha", new BigDecimal("120.00"));

        ReparoAdicional reparo = ReparoAdicional.criar("OS-123", 20L, List.of(servico));

        assertEquals("OS-123", reparo.getNumeroOs());
        assertEquals(20L, reparo.getMecanicoId());
        assertEquals(StatusReparoAdicional.PENDENTE_APROVACAO, reparo.getStatus());
        assertNotNull(reparo.getCriadoEm());
        assertEquals(List.of(servico), reparo.getServicos());
        assertSame(reparo, servico.getReparoAdicional());
    }

    @Test
    void deveAprovarReparoPendente() {
        ReparoAdicional reparo = ReparoAdicional.criar(
                "OS-123",
                20L,
                List.of(new ServicoSolicitado(1L, "Troca de pastilha", new BigDecimal("120.00")))
        );

        reparo.aprovar();

        assertEquals(StatusReparoAdicional.APROVADO, reparo.getStatus());
        assertNotNull(reparo.getAprovadoEm());
        assertNull(reparo.getRecusadoEm());
    }

    @Test
    void deveRecusarReparoPendenteComMotivo() {
        ReparoAdicional reparo = ReparoAdicional.criar(
                "OS-123",
                20L,
                List.of(new ServicoSolicitado(1L, "Troca de pastilha", new BigDecimal("120.00")))
        );

        reparo.recusar("Cliente recusou");

        assertEquals(StatusReparoAdicional.RECUSADO, reparo.getStatus());
        assertEquals("Cliente recusou", reparo.getMotivoRecusa());
        assertNotNull(reparo.getRecusadoEm());
        assertNull(reparo.getAprovadoEm());
    }

    @Test
    void naoDeveAprovarReparoQueNaoEstaPendente() {
        ReparoAdicional reparo = ReparoAdicional.criar(
                "OS-123",
                20L,
                List.of(new ServicoSolicitado(1L, "Troca de pastilha", new BigDecimal("120.00")))
        );
        reparo.recusar("Cliente recusou");

        assertThrows(IllegalStateException.class, reparo::aprovar);
    }

    @Test
    void naoDeveRecusarReparoQueNaoEstaPendente() {
        ReparoAdicional reparo = ReparoAdicional.criar(
                "OS-123",
                20L,
                List.of(new ServicoSolicitado(1L, "Troca de pastilha", new BigDecimal("120.00")))
        );
        reparo.aprovar();

        assertThrows(IllegalStateException.class, () -> reparo.recusar("Cliente recusou"));
    }
}
