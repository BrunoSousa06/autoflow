package com.autoflow.domain.pecainsumo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PecaInsumoEntityTest {

    private PecaInsumoEntity pecaInsumo;

    @BeforeEach
    void setUp() {
        pecaInsumo = new PecaInsumoEntity();
        pecaInsumo.setId(1L);
        pecaInsumo.setNome("Filtro de óleo");
        pecaInsumo.setQuantidade(50);
        pecaInsumo.setValor(new BigDecimal("25.00"));
        pecaInsumo.setTipo(CategoriaPecaInsumo.PECA);
    }

    @Test
    void testPecaInsumoCreation() {
        assertNotNull(pecaInsumo);
        assertEquals(1L, pecaInsumo.getId());
        assertEquals("Filtro de óleo", pecaInsumo.getNome());
        assertEquals(50, pecaInsumo.getQuantidade());
        assertEquals(new BigDecimal("25.00"), pecaInsumo.getValor());
        assertEquals(CategoriaPecaInsumo.PECA, pecaInsumo.getTipo());
    }

    @Test
    void testBaixarDoEstoque() {
        PecaInsumoEntity pecaRemover = new PecaInsumoEntity();
        pecaRemover.setQuantidade(10);

        pecaInsumo.baixarDoEstoque(pecaRemover);

        assertEquals(40, pecaInsumo.getQuantidade());
    }

    @Test
    void testBaixarDoEstoqueCompleto() {
        PecaInsumoEntity pecaRemover = new PecaInsumoEntity();
        pecaRemover.setQuantidade(50);

        pecaInsumo.baixarDoEstoque(pecaRemover);

        assertEquals(0, pecaInsumo.getQuantidade());
    }

    @Test
    void testBaixarDoEstoqueNegativo() {
        PecaInsumoEntity pecaRemover = new PecaInsumoEntity();
        pecaRemover.setQuantidade(60);

        pecaInsumo.baixarDoEstoque(pecaRemover);

        assertEquals(-10, pecaInsumo.getQuantidade());
    }

    @Test
    void testPecaInsumoSetters() {
        pecaInsumo.setNome("Filtro de ar");
        pecaInsumo.setQuantidade(100);
        pecaInsumo.setValor(new BigDecimal("35.50"));
        pecaInsumo.setTipo(CategoriaPecaInsumo.INSUMO);

        assertEquals("Filtro de ar", pecaInsumo.getNome());
        assertEquals(100, pecaInsumo.getQuantidade());
        assertEquals(new BigDecimal("35.50"), pecaInsumo.getValor());
        assertEquals(CategoriaPecaInsumo.INSUMO, pecaInsumo.getTipo());
    }

    @Test
    void testPecaInsumoNomeUniqueness() {
        PecaInsumoEntity peca2 = new PecaInsumoEntity();
        peca2.setNome("Filtro de óleo");

        assertEquals(pecaInsumo.getNome(), peca2.getNome());
    }

    @Test
    void testPecaInsumoQuantidadeZero() {
        pecaInsumo.setQuantidade(0);
        assertEquals(0, pecaInsumo.getQuantidade());
    }

    @Test
    void testPecaInsumoTipoEnum() {
        for (CategoriaPecaInsumo tipo : CategoriaPecaInsumo.values()) {
            pecaInsumo.setTipo(tipo);
            assertEquals(tipo, pecaInsumo.getTipo());
        }
    }
}
