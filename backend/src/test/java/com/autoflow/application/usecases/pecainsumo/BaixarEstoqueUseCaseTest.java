package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.EstoqueGateway;
import com.autoflow.application.dto.pecainsumo.EstoqueItemOutput;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaixarEstoqueUseCaseTest {

    @Mock
    private EstoqueGateway gateway;

    @Test
    void deveRetornarListaVaziaSemConsultarEstoque() {
        var useCase = new BaixarEstoqueUseCase(gateway);

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

        assertThrows(IllegalStateException.class, () -> new BaixarEstoqueUseCase(gateway).execute(List.of(
                item(1L, 3),
                item(2L, 2))));

        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveBaixarTodosOsItensQuandoEstoqueForSuficiente() {
        when(gateway.findAllByIdForUpdate(List.of(1L, 2L)))
                .thenReturn(List.of(estoque(1L, 5), estoque(2L, 3)));

        var resultado = new BaixarEstoqueUseCase(gateway).execute(List.of(
                item(1L, 3), item(2L, 2)));

        assertEquals(2, resultado.size());
        assertEquals(StatusItemNecessario.UTILIZADO, resultado.get(0).getStatus());
        assertEquals(StatusItemNecessario.UTILIZADO, resultado.get(1).getStatus());
        verify(gateway).saveAll(List.of(estoque(1L, 2), estoque(2L, 1)));
    }

    @Test
    void deveFalharQuandoPecaNaoExisteNoEstoque() {
        when(gateway.findAllByIdForUpdate(List.of(9L))).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> new BaixarEstoqueUseCase(gateway).execute(List.of(item(9L, 1))));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveValidarTodosOsItensAntesDeAlterarEstoque() {
        EstoqueItemOutput existente = estoque(1L, 5);
        when(gateway.findAllByIdForUpdate(List.of(1L, 9L))).thenReturn(List.of(existente));

        assertThrows(ResponseStatusException.class,
                () -> new BaixarEstoqueUseCase(gateway).execute(List.of(
                        item(1L, 2),
                        item(9L, 1)
                )));

        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveRejeitarItensRepetidosDoMesmoEstoque() {
        assertThrows(IllegalArgumentException.class, () -> new BaixarEstoqueUseCase(gateway).execute(List.of(
                item(1L, 3), item(1L, 3))));
        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveRejeitarItemNuloAntesDeConsultarEstoque() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaixarEstoqueUseCase(gateway)
                        .execute(java.util.Collections.singletonList(null)));

        verify(gateway, never()).findAllByIdForUpdate(any());
    }

    @Test
    void deveRejeitarItemSemPecaAntesDeConsultarEstoque() {
        assertThrows(IllegalArgumentException.class,
                () -> new BaixarEstoqueUseCase(gateway)
                        .execute(List.of(item(null, 1))));

        verify(gateway, never()).findAllByIdForUpdate(any());
    }

    @Test
    void deveRejeitarItemSemQuantidadeAntesDeConsultarEstoque() {
        var item = new ItemNecessarioEntity();
        item.setPecaInsumoId(1L);

        assertThrows(IllegalArgumentException.class,
                () -> new BaixarEstoqueUseCase(gateway).execute(List.of(item)));

        verify(gateway, never()).findAllByIdForUpdate(any());
    }

    @Test
    void naoDevePersistirQuandoNenhumItemPuderSerBaixado() {
        when(gateway.findAllByIdForUpdate(List.of(1L))).thenReturn(List.of(estoque(1L, 0)));

        assertThrows(IllegalStateException.class,
                () -> new BaixarEstoqueUseCase(gateway).execute(List.of(item(1L, 1))));
        verify(gateway, never()).saveAll(any());
    }

    private static ItemNecessarioEntity item(Long id, int quantidade) {
        return ItemNecessarioEntity.criar(id, "Item", CategoriaPecaInsumo.PECA,
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
