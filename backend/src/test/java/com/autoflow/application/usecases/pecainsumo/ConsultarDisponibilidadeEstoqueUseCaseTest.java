package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.EstoqueItemOutput;
import com.autoflow.application.exception.EstoqueItemNaoEncontradoException;
import com.autoflow.application.gateway.EstoqueGateway;
import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
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
class ConsultarDisponibilidadeEstoqueUseCaseTest {

    @Mock
    private EstoqueGateway gateway;

    @Test
    void deveMarcarItemComoDisponivelSemBaixarEstoque() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        var solicitado = itemSolicitado(1L, 3);
        var estoque = itemEstoque(1L, 5);
        when(gateway.findAllById(List.of(1L))).thenReturn(List.of(estoque));

        var resultado = useCase.execute(List.of(solicitado));

        assertAll(
                () -> assertEquals(StatusItemNecessario.DISPONIVEL, resultado.getFirst().getStatus()),
                () -> assertNull(resultado.getFirst().getMotivoPendencia()),
                () -> assertEquals(5, resultado.getFirst().getQuantidadeDisponivel()),
                 () -> assertEquals(5, estoque.quantidade()));
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveConsiderarDisponivelQuandoEstoqueForIgualAoSolicitado() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        var solicitado = itemSolicitado(1L, 5);
        var estoque = itemEstoque(1L, 5);
        when(gateway.findAllById(List.of(1L))).thenReturn(List.of(estoque));

        var resultado = useCase.execute(List.of(solicitado));

        assertAll(
                () -> assertEquals(StatusItemNecessario.DISPONIVEL, resultado.getFirst().getStatus()),
                () -> assertNull(resultado.getFirst().getMotivoPendencia()),
                () -> assertEquals(5, resultado.getFirst().getQuantidadeDisponivel()),
                 () -> assertEquals(5, estoque.quantidade()));
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveMarcarItemComoPendenteQuandoEstoqueForInsuficiente() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        var solicitado = itemSolicitado(1L, 6);
        var estoque = itemEstoque(1L, 5);
        when(gateway.findAllById(List.of(1L))).thenReturn(List.of(estoque));

        var resultado = useCase.execute(List.of(solicitado));

        assertAll(
                () -> assertEquals(StatusItemNecessario.PENDENTE, resultado.getFirst().getStatus()),
                () -> assertEquals(MotivoPendenciaItem.ESTOQUE_INSUFICIENTE,
                        resultado.getFirst().getMotivoPendencia()),
                () -> assertEquals(5, resultado.getFirst().getQuantidadeDisponivel()),
                 () -> assertEquals(5, estoque.quantidade()));
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveRejeitarPecaDuplicadaAntesDeConsultarEstoque() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        var primeiro = itemSolicitado(1L, 2);
        var segundo = itemSolicitado(1L, 3);
        var itensDuplicados = List.of(primeiro, segundo);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(itensDuplicados));

        verify(gateway, never()).findAllById(any());
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveRetornarListaVaziaSemConsultarEstoqueParaEntradaNulaOuVazia() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);

        assertTrue(useCase.execute(null).isEmpty());
        assertTrue(useCase.execute(List.of()).isEmpty());

        verify(gateway, never()).findAllById(any());
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveInformarItemInexistenteSemPersistirAlteracoes() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        when(gateway.findAllById(List.of(9L))).thenReturn(List.of());
        var itens = List.of(itemSolicitado(9L, 1));

        assertThrows(
                EstoqueItemNaoEncontradoException.class,
                () -> useCase.execute(itens));

        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveRejeitarQuantidadeNaoPositiva() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        when(gateway.findAllById(List.of(1L))).thenReturn(List.of(itemEstoque(1L, 5)));
        var itens = List.of(itemSolicitado(1L, 0));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(itens));
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveRejeitarItemNuloAntesDeConsultarEstoque() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        var itens = java.util.Collections.singletonList((ItemNecessario) null);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(itens));

        verify(gateway, never()).findAllById(any());
    }

    @Test
    void deveRejeitarItemSemPecaAntesDeConsultarEstoque() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        var itens = List.of(itemSolicitado(null, 1));

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(itens));

        verify(gateway, never()).findAllById(any());
    }

    @Test
    void deveRejeitarItemSemQuantidadeAntesDeConsultarEstoque() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCaseImpl(gateway);
        var item = new ItemNecessario();
        item.setPecaInsumoId(1L);
        var itens = List.of(item);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.execute(itens));

        verify(gateway, never()).findAllById(any());
    }

    private void verificarQueNaoHouvePersistencia() {
        verify(gateway, never()).saveAll(any());
    }

    private ItemNecessario itemSolicitado(Long id, int quantidade) {
        return ItemNecessario.criar(
                id,
                "Item solicitado",
                CategoriaPecaInsumo.PECA,
                BigDecimal.ONE,
                quantidade,
                null);
    }

    private EstoqueItemOutput itemEstoque(Long id, int quantidade) {
        return new EstoqueItemOutput(
                id,
                "Item " + id,
                CategoriaPecaInsumo.PECA,
                new BigDecimal("10.00"),
                quantidade
        );
    }
}
