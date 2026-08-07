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
    private PecaInsumoGateway gateway;

    @Test
    void deveRetornarListaVaziaSemConsultarEstoque() {
        var useCase = new BaixarEstoqueUseCase(gateway);

        assertTrue(useCase.execute(null).isEmpty());
        assertTrue(useCase.execute(List.of()).isEmpty());

        verify(gateway, never()).findAllById(any());
        verify(gateway, never()).saveAll(any());
    }

    @Test
    void deveBaixarItensDisponiveisEManterPendentes() {
        PecaInsumoEntity disponivel = estoque(1L, 5);
        PecaInsumoEntity insuficiente = estoque(2L, 1);
        when(gateway.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(disponivel, insuficiente));

        var resultado = new BaixarEstoqueUseCase(gateway).execute(List.of(
                item(1L, 3),
                item(2L, 2)));

        assertEquals(2, resultado.size());
        assertEquals(2, disponivel.getQuantidade());
        assertEquals(StatusItemNecessario.UTILIZADO, resultado.get(0).getStatus());
        assertEquals(1, insuficiente.getQuantidade());
        assertEquals(StatusItemNecessario.PENDENTE, resultado.get(1).getStatus());
        assertEquals(MotivoPendenciaItem.ESTOQUE_INSUFICIENTE,
                resultado.get(1).getMotivoPendencia());
        verify(gateway).saveAll(List.of(disponivel));
    }

    @Test
    void deveFalharQuandoPecaNaoExisteNoEstoque() {
        when(gateway.findAllById(List.of(9L))).thenReturn(List.of());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> new BaixarEstoqueUseCase(gateway).execute(List.of(item(9L, 1))));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(gateway, never()).saveAll(any());
    }

    private static ItemNecessarioEntity item(Long id, int quantidade) {
        return ItemNecessarioEntity.criar(id, "Item", CategoriaPecaInsumo.PECA,
                BigDecimal.ONE, quantidade, null);
    }

    private static PecaInsumoEntity estoque(Long id, int quantidade) {
        var estoque = new PecaInsumoEntity();
        estoque.setId(id);
        estoque.setNome("Item " + id);
        estoque.setTipo(CategoriaPecaInsumo.PECA);
        estoque.setValor(BigDecimal.ONE);
        estoque.setQuantidade(quantidade);
        return estoque;
    }
}
