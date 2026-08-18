package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.input.PageQuery;
import com.autoflow.application.output.PageResult;
import com.autoflow.application.input.pecainsumo.PecaInsumoInput;
import com.autoflow.application.output.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecaInsumoUseCasesTest {

    @Mock
    PecaInsumoGateway gateway;

    private final PecaInsumoInput input =
            new PecaInsumoInput("Filtro", new BigDecimal("50.00"), 10, CategoriaPecaInsumo.PECA);
    private final PecaInsumoOutput output =
            new PecaInsumoOutput(1L, "Filtro", new BigDecimal("50.00"), 10, CategoriaPecaInsumo.PECA);

    @Test
    void deveCadastrarEImpedirNomeDuplicado() {
        var useCase = new CadastrarPecaInsumoUseCaseImpl(gateway);
        when(gateway.findByNomeIgnoreCase("Filtro")).thenReturn(Optional.empty());
        when(gateway.save(input)).thenReturn(output);

        assertEquals(output, useCase.execute(input));

        when(gateway.findByNomeIgnoreCase("Filtro")).thenReturn(Optional.of(output));
        assertThrows(ApplicationException.class, () -> useCase.execute(input));
    }

    @Test
    void deveBuscarPorIdEInformarAusencia() {
        var useCase = new BuscarPecaInsumoPorIdUseCaseImpl(gateway);
        when(gateway.findById(1L)).thenReturn(Optional.of(output));
        assertEquals(output, useCase.execute(1L));

        when(gateway.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> useCase.execute(2L));
    }

    @Test
    void deveAtualizarItemExistente() {
        var useCase = new AtualizarPecaInsumoUseCaseImpl(gateway);
        when(gateway.findById(1L)).thenReturn(Optional.of(output));
        when(gateway.update(1L, input)).thenReturn(output);

        assertEquals(output, useCase.execute(1L, input));
        verify(gateway).update(1L, input);

        when(gateway.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ApplicationException.class, () -> useCase.execute(2L, input));
    }

    @Test
    void deveListarEListarPaginado() {
        when(gateway.findAll()).thenReturn(List.of(output));
        assertEquals(List.of(output), new ListarPecaInsumoUseCaseImpl(gateway).execute());

        var pageQuery = new PageQuery(0, 10);
        when(gateway.findAll(any(), eq(pageQuery)))
                .thenReturn(new PageResult<>(List.of(output), 1, 0, 10));
        assertEquals(List.of(output), new ListarPecaInsumoPaginadoUseCaseImpl(gateway)
                .execute(pageQuery, "Fil", CategoriaPecaInsumo.PECA).content());
    }

    @Test
    void deveDeletarSomenteItemExistente() {
        var useCase = new DeletarPecaInsumoUseCaseImpl(gateway);
        when(gateway.existsById(1L)).thenReturn(true);
        useCase.execute(1L);
        verify(gateway).deleteById(1L);

        when(gateway.existsById(2L)).thenReturn(false);
        assertThrows(ApplicationException.class, () -> useCase.execute(2L));
    }
}
