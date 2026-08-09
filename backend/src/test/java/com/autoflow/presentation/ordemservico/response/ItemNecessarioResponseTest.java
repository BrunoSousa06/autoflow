package com.autoflow.presentation.ordemservico.response;

import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ItemNecessarioResponseTest {

    @Test
    void deveCriarResponseDiretamente() {
        ItemNecessarioResponse response = new ItemNecessarioResponse(
                1L, "Filtro de óleo", CategoriaPecaInsumo.PECA,
                BigDecimal.valueOf(50), 2, BigDecimal.valueOf(100),
                StatusItemNecessario.DISPONIVEL, null, 10, null
        );

        assertEquals(1L, response.pecaInsumoId());
        assertEquals("Filtro de óleo", response.nome());
        assertEquals(CategoriaPecaInsumo.PECA, response.tipo());
        assertEquals(BigDecimal.valueOf(50), response.valorUnitario());
        assertEquals(2, response.quantidade());
        assertEquals(BigDecimal.valueOf(100), response.valorTotal());
        assertEquals(StatusItemNecessario.DISPONIVEL, response.status());
        assertNull(response.motivoPendencia());
        assertEquals(10, response.quantidadeDisponivel());
        assertNull(response.mensagemStatus());
    }

    @Test
    void deveConverterDeDomainComItemDisponivel() {
        ItemNecessarioEntity entity = ItemNecessarioEntity.criar(
                1L, "Filtro de óleo", CategoriaPecaInsumo.PECA,
                BigDecimal.valueOf(50), 2, StatusItemNecessario.DISPONIVEL
        );

        ItemNecessarioResponse response = ItemNecessarioResponse.fromDomain(entity);

        assertEquals(1L, response.pecaInsumoId());
        assertEquals("Filtro de óleo", response.nome());
        assertEquals(CategoriaPecaInsumo.PECA, response.tipo());
        assertEquals(BigDecimal.valueOf(50), response.valorUnitario());
        assertEquals(2, response.quantidade());
        assertEquals(StatusItemNecessario.DISPONIVEL, response.status());
        assertNull(response.motivoPendencia());
        assertNull(response.mensagemStatus());
    }

    @Test
    void deveConverterDeDomainComItemPendentePorEstoqueInsuficiente() {
        ItemNecessarioEntity entity = ItemNecessarioEntity.criar(
                2L, "Pastilha de freio", CategoriaPecaInsumo.PECA,
                BigDecimal.valueOf(80), 4, StatusItemNecessario.PENDENTE,
                new com.autoflow.domain.ordemservico.SituacaoEstoque(1, MotivoPendenciaItem.ESTOQUE_INSUFICIENTE)
        );

        ItemNecessarioResponse response = ItemNecessarioResponse.fromDomain(entity);

        assertEquals(StatusItemNecessario.PENDENTE, response.status());
        assertEquals(MotivoPendenciaItem.ESTOQUE_INSUFICIENTE, response.motivoPendencia());
        assertEquals(1, response.quantidadeDisponivel());
        assertNotNull(response.mensagemStatus());
        assertTrue(response.mensagemStatus().contains("Estoque insuficiente"));
    }

    @Test
    void deveSerIgualQuandoMesmosValores() {
        ItemNecessarioResponse r1 = new ItemNecessarioResponse(1L, "Filtro", CategoriaPecaInsumo.INSUMO,
                BigDecimal.TEN, 1, BigDecimal.TEN, StatusItemNecessario.DISPONIVEL, null, 5, null);
        ItemNecessarioResponse r2 = new ItemNecessarioResponse(1L, "Filtro", CategoriaPecaInsumo.INSUMO,
                BigDecimal.TEN, 1, BigDecimal.TEN, StatusItemNecessario.DISPONIVEL, null, 5, null);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
