package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.PageResult;
import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.application.mapper.PecaInsumoMapper;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecaInsumoUseCasesTest {

    @Mock PecaInsumoGateway gateway;
    @Mock PecaInsumoMapper mapper;

    private PecaInsumoEntity entity;
    private PecaInsumoInput input;
    private PecaInsumoOutput output;

    @BeforeEach
    void setUp() {
        entity = new PecaInsumoEntity();
        entity.setId(1L);
        entity.setNome("Filtro");
        entity.setTipo(CategoriaPecaInsumo.PECA);
        entity.setValor(new BigDecimal("50.00"));
        entity.setQuantidade(10);
        input = new PecaInsumoInput("Filtro", new BigDecimal("50.00"), 10, CategoriaPecaInsumo.PECA);
        output = new PecaInsumoOutput(1L, "Filtro", new BigDecimal("50.00"), 10, CategoriaPecaInsumo.PECA);
    }

    @Test
    void deveCadastrarEImpedirNomeDuplicado() {
        var useCase = new CadastrarPecaInsumoUseCase(gateway, mapper);
        when(gateway.findByNomeIgnoreCase("Filtro")).thenReturn(Optional.empty());
        when(mapper.mapToEntity(input)).thenReturn(entity);
        when(gateway.save(entity)).thenReturn(entity);
        when(mapper.mapToOutput(entity)).thenReturn(output);
        assertEquals(output, useCase.execute(input));

        when(gateway.findByNomeIgnoreCase("Filtro")).thenReturn(Optional.of(entity));
        assertThrows(ApplicationException.class, () -> useCase.execute(input));
    }

    @Test
    void deveBuscarPorIdEInformarAusencia() {
        var useCase = new BuscarPecaInsumoPorIdUseCase(gateway, mapper);
        when(gateway.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.mapToOutput(entity)).thenReturn(output);
        assertEquals(output, useCase.execute(1L));

        when(gateway.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> useCase.execute(2L));
    }

    @Test
    void deveAtualizarItemExistente() {
        var buscar = new BuscarEAtualizarPecaInsumoPorIdUseCase(gateway);
        var useCase = new AtualizarPecaInsumoUseCase(buscar, gateway, mapper);
        when(gateway.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.mapToOutput(entity)).thenReturn(output);
        assertEquals(output, useCase.execute(1L, input));
        verify(mapper).updateEntity(input, entity);
        verify(gateway).save(entity);

        when(gateway.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ApplicationException.class, () -> buscar.execute(2L));
    }

    @Test
    void deveListarEListarPaginado() {
        when(gateway.findAll()).thenReturn(List.of(entity));
        when(mapper.mapToOutput(entity)).thenReturn(output);
        assertEquals(List.of(output), new ListarPecaInsumoUseCase(gateway, mapper).execute());

        var pageQuery = new PageQuery(0, 10);
        when(gateway.findAll(any(), eq(pageQuery))).thenReturn(new PageResult<>(List.of(entity), 1, 0, 10));
        assertEquals(List.of(output),
                new ListarPecaInsumoPaginadoUseCase(gateway, mapper)
                        .execute(pageQuery, "Fil", CategoriaPecaInsumo.PECA).content());
    }

    @Test
    void deveDeletarSomenteItemExistente() {
        var useCase = new DeletarPecaInsumoUseCase(gateway);
        when(gateway.existsById(1L)).thenReturn(true);
        useCase.execute(1L);
        verify(gateway).deleteById(1L);

        when(gateway.existsById(2L)).thenReturn(false);
        assertThrows(ApplicationException.class, () -> useCase.execute(2L));
    }

}
