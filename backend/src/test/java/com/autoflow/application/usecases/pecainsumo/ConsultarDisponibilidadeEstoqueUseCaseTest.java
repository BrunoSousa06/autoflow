package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadeEstoqueUseCaseTest {

    @Mock
    private PecaInsumoGateway gateway;

    @Test
    void deveMarcarItemComoDisponivelSemBaixarEstoque() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCase(gateway);
        var solicitado = itemSolicitado(1L, 3);
        var estoque = itemEstoque(1L, 5);
        when(gateway.findAllById(List.of(1L))).thenReturn(List.of(estoque));

        var resultado = useCase.execute(List.of(solicitado));

        assertAll(
                () -> assertEquals(StatusItemNecessario.DISPONIVEL, resultado.getFirst().getStatus()),
                () -> assertNull(resultado.getFirst().getMotivoPendencia()),
                () -> assertEquals(5, resultado.getFirst().getQuantidadeDisponivel()),
                () -> assertEquals(5, estoque.getQuantidade()));
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveConsiderarDisponivelQuandoEstoqueForIgualAoSolicitado() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCase(gateway);
        var solicitado = itemSolicitado(1L, 5);
        var estoque = itemEstoque(1L, 5);
        when(gateway.findAllById(List.of(1L))).thenReturn(List.of(estoque));

        var resultado = useCase.execute(List.of(solicitado));

        assertAll(
                () -> assertEquals(StatusItemNecessario.DISPONIVEL, resultado.getFirst().getStatus()),
                () -> assertNull(resultado.getFirst().getMotivoPendencia()),
                () -> assertEquals(5, resultado.getFirst().getQuantidadeDisponivel()),
                () -> assertEquals(5, estoque.getQuantidade()));
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveMarcarItemComoPendenteQuandoEstoqueForInsuficiente() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCase(gateway);
        var solicitado = itemSolicitado(1L, 6);
        var estoque = itemEstoque(1L, 5);
        when(gateway.findAllById(List.of(1L))).thenReturn(List.of(estoque));

        var resultado = useCase.execute(List.of(solicitado));

        assertAll(
                () -> assertEquals(StatusItemNecessario.PENDENTE, resultado.getFirst().getStatus()),
                () -> assertEquals(MotivoPendenciaItem.ESTOQUE_INSUFICIENTE,
                        resultado.getFirst().getMotivoPendencia()),
                () -> assertEquals(5, resultado.getFirst().getQuantidadeDisponivel()),
                () -> assertEquals(5, estoque.getQuantidade()));
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveBuscarIdsDistintosEmUmaUnicaConsulta() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCase(gateway);
        var primeiro = itemSolicitado(1L, 2);
        var segundo = itemSolicitado(1L, 3);
        var estoque = itemEstoque(1L, 5);
        when(gateway.findAllById(List.of(1L))).thenReturn(List.of(estoque));

        var resultado = useCase.execute(List.of(primeiro, segundo));

        assertEquals(2, resultado.size());
        verify(gateway).findAllById(List.of(1L));
        assertEquals(5, estoque.getQuantidade());
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveRetornarListaVaziaSemConsultarEstoqueParaEntradaNulaOuVazia() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCase(gateway);

        assertTrue(useCase.execute(null).isEmpty());
        assertTrue(useCase.execute(List.of()).isEmpty());

        verify(gateway, never()).findAllById(any());
        verificarQueNaoHouvePersistencia();
    }

    @Test
    void deveInformarItemInexistenteSemPersistirAlteracoes() {
        var useCase = new ConsultarDisponibilidadeEstoqueUseCase(gateway);
        when(gateway.findAllById(List.of(9L))).thenReturn(List.of());

        var erro = assertThrows(
                ResponseStatusException.class,
                () -> useCase.execute(List.of(itemSolicitado(9L, 1))));

        assertEquals(HttpStatus.NOT_FOUND, erro.getStatusCode());
        verificarQueNaoHouvePersistencia();
    }

    private void verificarQueNaoHouvePersistencia() {
        verify(gateway, never()).save(any());
        verify(gateway, never()).saveAll(any());
    }

    private ItemNecessarioEntity itemSolicitado(Long id, int quantidade) {
        return ItemNecessarioEntity.criar(
                id,
                "Item solicitado",
                CategoriaPecaInsumo.PECA,
                BigDecimal.ONE,
                quantidade,
                null);
    }

    private PecaInsumoEntity itemEstoque(Long id, int quantidade) {
        var estoque = new PecaInsumoEntity();
        estoque.setId(id);
        estoque.setNome("Item " + id);
        estoque.setTipo(CategoriaPecaInsumo.PECA);
        estoque.setValor(new BigDecimal("10.00"));
        estoque.setQuantidade(quantidade);
        return estoque;
    }
}
