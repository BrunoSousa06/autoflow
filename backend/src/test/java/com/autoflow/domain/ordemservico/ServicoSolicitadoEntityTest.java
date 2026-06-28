package com.autoflow.domain.ordemservico;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicoSolicitadoEntityTest {

    private ServicoSolicitadoEntity servicoSolicitado;

    @BeforeEach
    void setUp() {
        servicoSolicitado = ServicoSolicitadoEntity.criar(
                1L,
                "Troca de óleo",
                new BigDecimal("150.00")
        );
    }

    @Test
    void testServicoSolicitadoCreation() {
        assertNotNull(servicoSolicitado);
        assertEquals(1L, servicoSolicitado.getServicoId());
        assertEquals("Troca de óleo", servicoSolicitado.getNome());
        assertEquals(new BigDecimal("150.00"), servicoSolicitado.getValor());
        assertEquals(StatusServicoOs.AGUARDANDO, servicoSolicitado.getStatus());
    }

    @Test
    void testServicoSolicitadoConstructorWithServicoId() {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L);
        assertEquals(1L, servico.getServicoId());
    }

    @Test
    void testServicoSolicitadoConstructorWithNome() {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisão");
        assertEquals(1L, servico.getServicoId());
        assertEquals("Revisão", servico.getNome());
    }

    @Test
    void testServicoSolicitadoConstructorComplete() {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(
                2L,
                "Alinhamento",
                new BigDecimal("200.00")
        );
        assertEquals(2L, servico.getServicoId());
        assertEquals("Alinhamento", servico.getNome());
        assertEquals(new BigDecimal("200.00"), servico.getValor());
    }

    @Test
    void testServicoSolicitadoInvalidServicoId() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ServicoSolicitadoEntity(null);
        });
    }

    @Test
    void testServicoSolicitadoInvalidNome() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ServicoSolicitadoEntity(1L, "");
        });
    }

    @Test
    void testServicoSolicitadoInvalidValor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ServicoSolicitadoEntity(1L, "Serviço", null);
        });
    }

    @Test
    void testRegistrarItensNecessarios() {
        List<ItemNecessarioEntity> itens = new ArrayList<>();
        ItemNecessarioEntity item = new ItemNecessarioEntity();
        item.setNome("Óleo sintético");
        itens.add(item);

        servicoSolicitado.registrarItensNecessarios(itens);

        assertEquals(1, servicoSolicitado.getItensNecessarios().size());
        assertEquals("Óleo sintético", servicoSolicitado.getItensNecessarios().get(0).getNome());
    }

    @Test
    void testIniciarServico() {
        List<ItemNecessarioEntity> itens = new ArrayList<>();
        servicoSolicitado.iniciar(itens);

        assertEquals(StatusServicoOs.EM_EXECUCAO, servicoSolicitado.getStatus());
        assertNotNull(servicoSolicitado.getIniciadoEm());
    }

    @Test
    void testIniciarServicoJaIniciado() {
        List<ItemNecessarioEntity> itens = new ArrayList<>();
        servicoSolicitado.iniciar(itens);

        assertThrows(IllegalStateException.class, () -> {
            servicoSolicitado.iniciar(itens);
        });
    }

    @Test
    void testFinalizarServico() {
        List<ItemNecessarioEntity> itens = new ArrayList<>();
        servicoSolicitado.iniciar(itens);
        servicoSolicitado.finalizar();

        assertEquals(StatusServicoOs.FINALIZADO, servicoSolicitado.getStatus());
        assertNotNull(servicoSolicitado.getFinalizadoEm());
    }

    @Test
    void testFinalizarServicoNaoIniciado() {
        assertThrows(IllegalStateException.class, () -> {
            servicoSolicitado.finalizar();
        });
    }

    @Test
    void testServicoSolicitadoSetters() {
        servicoSolicitado.setStatus(StatusServicoOs.EM_EXECUCAO);
        servicoSolicitado.setIniciadoEm(LocalDateTime.now());

        assertEquals(StatusServicoOs.EM_EXECUCAO, servicoSolicitado.getStatus());
        assertNotNull(servicoSolicitado.getIniciadoEm());
    }

    @Test
    void testServicoSolicitadoReparoAdicionalAssociation() {
        ReparoAdicionalEntity reparo = new ReparoAdicionalEntity();
        servicoSolicitado.setReparoAdicional(reparo);

        assertNotNull(servicoSolicitado.getReparoAdicional());
    }

    @Test
    void testServicoSolicitadoDefaultStatus() {
        ServicoSolicitadoEntity novoServico = new ServicoSolicitadoEntity();
        assertEquals(StatusServicoOs.AGUARDANDO, novoServico.getStatus());
    }

    @Test
    void testServicoSolicitadoItensNecessariosCollection() {
        assertNotNull(servicoSolicitado.getItensNecessarios());
        assertTrue(servicoSolicitado.getItensNecessarios().isEmpty());
    }
}
