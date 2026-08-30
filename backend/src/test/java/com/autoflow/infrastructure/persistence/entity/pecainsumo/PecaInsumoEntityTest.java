package com.autoflow.infrastructure.persistence.entity.pecainsumo;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    void deveArmazenarDadosDaPeca() {
        assertNotNull(pecaInsumo);
        assertEquals(1L, pecaInsumo.getId());
        assertEquals("Filtro de óleo", pecaInsumo.getNome());
        assertEquals(50, pecaInsumo.getQuantidade());
        assertEquals(new BigDecimal("25.00"), pecaInsumo.getValor());
        assertEquals(CategoriaPecaInsumo.PECA, pecaInsumo.getTipo());
    }

    @Test
    void deveAtualizarDadosDaPeca() {
        pecaInsumo.setNome("Filtro de ar");
        pecaInsumo.setQuantidade(100);
        pecaInsumo.setValor(new BigDecimal("35.50"));
        pecaInsumo.setTipo(CategoriaPecaInsumo.INSUMO);

        assertEquals("Filtro de ar", pecaInsumo.getNome());
        assertEquals(100, pecaInsumo.getQuantidade());
        assertEquals(new BigDecimal("35.50"), pecaInsumo.getValor());
        assertEquals(CategoriaPecaInsumo.INSUMO, pecaInsumo.getTipo());
    }
}
