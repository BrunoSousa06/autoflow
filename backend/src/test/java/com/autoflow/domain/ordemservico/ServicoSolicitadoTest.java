package com.autoflow.domain.ordemservico;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServicoSolicitadoTest {

    private ServicoSolicitado servicoSolicitado;

    @BeforeEach
    void setUp() {
        servicoSolicitado = ServicoSolicitado.criar(
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
        ServicoSolicitado servico = new ServicoSolicitado(1L);
        assertEquals(1L, servico.getServicoId());
    }

    @Test
    void testServicoSolicitadoConstructorWithNome() {
        ServicoSolicitado servico = new ServicoSolicitado(1L, "Revisão");
        assertEquals(1L, servico.getServicoId());
        assertEquals("Revisão", servico.getNome());
    }

    @Test
    void testServicoSolicitadoConstructorComplete() {
        ServicoSolicitado servico = new ServicoSolicitado(
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
            new ServicoSolicitado(null);
        });
    }

    @Test
    void testServicoSolicitadoInvalidNome() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ServicoSolicitado(1L, "");
        });
    }

    @Test
    void testServicoSolicitadoInvalidValor() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ServicoSolicitado(1L, "Serviço", null);
        });
    }

    @Test
    void testRegistrarItensNecessarios() {
        List<ItemNecessario> itens = new ArrayList<>();
        ItemNecessario item = new ItemNecessario();
        item.setNome("Óleo sintético");
        itens.add(item);

        servicoSolicitado.registrarItensNecessarios(itens);

        assertEquals(1, servicoSolicitado.getItensNecessarios().size());
        assertEquals("Óleo sintético", servicoSolicitado.getItensNecessarios().get(0).getNome());
    }

    @Test
    void testIniciarServico() {
        List<ItemNecessario> itens = new ArrayList<>();
        servicoSolicitado.iniciar(itens);

        assertEquals(StatusServicoOs.EM_EXECUCAO, servicoSolicitado.getStatus());
        assertNotNull(servicoSolicitado.getIniciadoEm());
    }

    @Test
    void testIniciarServicoJaIniciado() {
        List<ItemNecessario> itens = new ArrayList<>();
        servicoSolicitado.iniciar(itens);

        assertThrows(IllegalStateException.class, () -> {
            servicoSolicitado.iniciar(itens);
        });
    }

    @Test
    void testIniciarServicoComItensReconstruidosComoListaImutavel() {
        servicoSolicitado.setItensNecessarios(List.of(new ItemNecessario()));

        servicoSolicitado.iniciar(List.of(new ItemNecessario()));

        assertEquals(StatusServicoOs.EM_EXECUCAO, servicoSolicitado.getStatus());
    }

    @Test
    void testFinalizarServico() {
        List<ItemNecessario> itens = new ArrayList<>();
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
    void testServicoSolicitadoDefaultStatus() {
        ServicoSolicitado novoServico = new ServicoSolicitado();
        assertEquals(StatusServicoOs.AGUARDANDO, novoServico.getStatus());
    }

    @Test
    void testServicoSolicitadoItensNecessariosCollection() {
        assertNotNull(servicoSolicitado.getItensNecessarios());
        assertTrue(servicoSolicitado.getItensNecessarios().isEmpty());
    }

    @Test
    void testRegistrarItensNecessarios_statusNaoAguardando_lanca() {
        List<ItemNecessario> itens = new ArrayList<>();
        servicoSolicitado.iniciar(itens);

        assertThrows(IllegalStateException.class, () -> servicoSolicitado.registrarItensNecessarios(itens));
    }

    @Test
    void testIniciar_comOrdemServicoNaoEmExecucao_lanca() {
        assertThrows(IllegalStateException.class,
                () -> servicoSolicitado.validarPodeIniciar(StatusOrdemServico.RECEBIDA));
    }

    @Test
    void testIniciar_comOrdemServicoEmExecucao_sucesso() {
        servicoSolicitado.iniciar(new ArrayList<>(), StatusOrdemServico.EM_EXECUCAO);

        assertEquals(StatusServicoOs.EM_EXECUCAO, servicoSolicitado.getStatus());
    }

    @Test
    void testValidarNome_nulo_lanca() {
        assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitado(1L, null));
    }

}
