package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.exception.EstoqueItemNaoEncontradoException;
import com.autoflow.application.gateway.EstoqueGateway;
import com.autoflow.application.output.pecainsumo.EstoqueItemOutput;
import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BaixarEstoqueUseCaseTest {

    @Mock
    private EstoqueGateway gateway;

    @Test
    void deveRetornarListaVaziaSemConsultarEstoque() {
        var useCase = new BaixarEstoqueUseCaseImpl(gateway);

        assertTrue(useCase.execute(null).isEmpty());
        assertTrue(useCase.execute(List.of()).isEmpty());

        verify(gateway, never()).findAllByIdForUpdate(any());
        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveBloquearBaixaQuandoQualquerItemEstiverInsuficiente() {
        EstoqueItemOutput disponivel = estoque(1L, 5);
        EstoqueItemOutput insuficiente = estoque(2L, 1);
        when(gateway.findAllByIdForUpdate(List.of(1L, 2L)))
                .thenReturn(List.of(disponivel, insuficiente));

        assertThrows(IllegalStateException.class, () -> new BaixarEstoqueUseCaseImpl(gateway).execute(List.of(
                item(1L, 3),
                item(2L, 2))));

        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveBaixarTodosOsItensQuandoEstoqueForSuficiente() {
        when(gateway.findAllByIdForUpdate(List.of(1L, 2L)))
                .thenReturn(List.of(estoque(1L, 5), estoque(2L, 3)));

        var resultado = new BaixarEstoqueUseCaseImpl(gateway).execute(List.of(
                item(1L, 3), item(2L, 2)));

        assertEquals(2, resultado.size());
        assertEquals(StatusItemNecessario.UTILIZADO, resultado.get(0).getStatus());
        assertEquals(StatusItemNecessario.UTILIZADO, resultado.get(1).getStatus());
        verify(gateway).saveAll(List.of(estoque(1L, 2), estoque(2L, 1)));
    }

    @Test
    void deveFalharQuandoPecaNaoExisteNoEstoque() {
        when(gateway.findAllByIdForUpdate(List.of(9L))).thenReturn(List.of());

        EstoqueItemNaoEncontradoException exception = assertThrows(EstoqueItemNaoEncontradoException.class,
                () -> new BaixarEstoqueUseCaseImpl(gateway).execute(List.of(item(9L, 1))));

        assertEquals("Peça/Insumo não encontrado com o ID: 9", exception.getMessage());
        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveValidarTodosOsItensAntesDeAlterarEstoque() {
        EstoqueItemOutput existente = estoque(1L, 5);
        when(gateway.findAllByIdForUpdate(List.of(1L, 9L))).thenReturn(List.of(existente));

        assertThrows(EstoqueItemNaoEncontradoException.class,
                () -> new BaixarEstoqueUseCaseImpl(gateway).execute(List.of(
                        item(1L, 2),
                        item(9L, 1)
                )));

        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveRejeitarItensRepetidosDoMesmoEstoque() {
        assertThrows(IllegalArgumentException.class, () -> new BaixarEstoqueUseCaseImpl(gateway).execute(List.of(
                item(1L, 3), item(1L, 3))));
        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveRejeitarItemNuloAntesDeConsultarEstoque() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaixarEstoqueUseCaseImpl(gateway)
                        .execute(java.util.Collections.singletonList(null)));

        verify(gateway, never()).findAllByIdForUpdate(any());
    }

    @Test
    void deveRejeitarItemSemPecaAntesDeConsultarEstoque() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaixarEstoqueUseCaseImpl(gateway)
                        .execute(List.of(item(null, 1))));

        verify(gateway, never()).findAllByIdForUpdate(any());
    }

    @Test
    void deveRejeitarItemSemQuantidadeAntesDeConsultarEstoque() {
        var item = new ItemNecessario();
        item.setPecaInsumoId(1L);

        assertThrows(IllegalArgumentException.class,
                () -> new BaixarEstoqueUseCaseImpl(gateway).execute(List.of(item)));

        verify(gateway, never()).findAllByIdForUpdate(any());
    }

    @Test
    void naoDevePersistirQuandoNenhumItemPuderSerBaixado() {
        when(gateway.findAllByIdForUpdate(List.of(1L))).thenReturn(List.of(estoque(1L, 0)));

        assertThrows(IllegalStateException.class,
                () -> new BaixarEstoqueUseCaseImpl(gateway).execute(List.of(item(1L, 1))));
        verify(gateway, never()).saveAll(any());
    }

    private static ItemNecessario item(Long id, int quantidade) {
        return ItemNecessario.criar(id, "Item", CategoriaPecaInsumo.PECA,
                BigDecimal.ONE, quantidade, null);
    }

    private static EstoqueItemOutput estoque(Long id, int quantidade) {
        return new EstoqueItemOutput(
                id,
                "Item " + id,
                CategoriaPecaInsumo.PECA,
                BigDecimal.ONE,
                quantidade
        );
    }
}
