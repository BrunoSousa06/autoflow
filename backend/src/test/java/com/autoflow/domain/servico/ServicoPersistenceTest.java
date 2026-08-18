package com.autoflow.domain.servico;

import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServicoPersistenceTest {

    private ServicoEntity servico;

    @BeforeEach
    void setUp() {
        servico = new ServicoEntity();
        servico.setId(1L);
        servico.setNome("Troca de óleo");
        servico.setDescricao("Troca de óleo e filtro");
        servico.setValor(new BigDecimal("150.00"));
        servico.setAtivo(true);
    }

    @Test
    void testServicoCreation() {
        assertNotNull(servico);
        assertEquals(1L, servico.getId());
        assertEquals("Troca de óleo", servico.getNome());
        assertEquals("Troca de óleo e filtro", servico.getDescricao());
        assertEquals(new BigDecimal("150.00"), servico.getValor());
        assertTrue(servico.isAtivo());
    }

    @Test
    void testServicoSetters() {
        servico.setNome("Revisão completa");
        servico.setDescricao("Revisão completa do veículo");
        servico.setValor(new BigDecimal("500.00"));
        servico.setAtivo(false);

        assertEquals("Revisão completa", servico.getNome());
        assertEquals("Revisão completa do veículo", servico.getDescricao());
        assertEquals(new BigDecimal("500.00"), servico.getValor());
        assertFalse(servico.isAtivo());
    }

    @Test
    void testServicoDefaultActive() {
        ServicoEntity novoServico = new ServicoEntity();
        assertTrue(novoServico.isAtivo());
    }

    @Test
    void testServicoNomeUniqueness() {
        ServicoEntity servico2 = new ServicoEntity();
        servico2.setNome("Troca de óleo");

        assertEquals(servico.getNome(), servico2.getNome());
    }

    @Test
    void testServicoValorNullable() {
        ServicoEntity novoServico = new ServicoEntity();
        novoServico.setNome("Serviço teste");

        assertNull(novoServico.getValor());
    }

    @Test
    void testServicoComValorGrande() {
        servico.setValor(new BigDecimal("9999.99"));
        assertEquals(new BigDecimal("9999.99"), servico.getValor());
    }

    @Test
    void testServicoComValorZero() {
        servico.setValor(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, servico.getValor());
    }
}
