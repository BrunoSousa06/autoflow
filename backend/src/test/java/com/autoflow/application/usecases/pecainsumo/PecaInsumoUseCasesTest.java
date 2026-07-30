package com.autoflow.application.usecases.pecainsumo;

import com.autoflow.application.dto.pecainsumo.PecaInsumoInput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoOutput;
import com.autoflow.application.gateway.PecaInsumoGateway;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoMapper;
import com.autoflow.presentation.pecainsumo.response.PecaInsumoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
        var erro = assertThrows(ResponseStatusException.class, () -> useCase.execute(input));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    @Test
    void deveBuscarPorIdEInformarAusencia() {
        var useCase = new BuscarPecaInsumoPorIdUseCase(gateway, mapper);
        when(gateway.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.mapToOutput(entity)).thenReturn(output);
        assertEquals(output, useCase.execute(1L));

        when(gateway.findById(2L)).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> useCase.execute(2L)).getStatusCode());
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
        assertThrows(ResponseStatusException.class, () -> buscar.execute(2L));
    }

    @Test
    void deveListarEListarPaginado() {
        var response = new PecaInsumoResponse(1L, "Filtro", new BigDecimal("50.00"), 10, CategoriaPecaInsumo.PECA);
        when(gateway.findAll()).thenReturn(List.of(entity));
        when(mapper.toResponseList(List.of(entity))).thenReturn(List.of(response));
        assertEquals(List.of(response), new ListarPecaInsumoUseCase(gateway, mapper).execute());

        var pageable = PageRequest.of(0, 10);
        when(gateway.findAll(any(), eq(pageable))).thenReturn(new PageImpl<>(List.of(entity)));
        when(mapper.mapToOutput(entity)).thenReturn(output);
        assertEquals(List.of(output),
                new ListarPecaInsumoPaginadoUseCase(gateway, mapper)
                        .execute(pageable, "Fil", CategoriaPecaInsumo.PECA).getContent());
    }

    @Test
    void deveDeletarSomenteItemExistente() {
        var useCase = new DeletarPecaInsumoUseCase(gateway);
        when(gateway.existsById(1L)).thenReturn(true);
        useCase.execute(1L);
        verify(gateway).deleteById(1L);

        when(gateway.existsById(2L)).thenReturn(false);
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class, () -> useCase.execute(2L)).getStatusCode());
    }

    @Test
    void deveBaixarEstoqueSuficienteEPreservarInsuficiente() {
        var useCase = new VerificarDisponibilidadeEBaixarEstoqueUseCase(gateway);
        var suficiente = item(1L, 3);
        var insuficiente = item(2L, 4);
        var estoqueInsuficiente = estoque(2L, 2);
        when(gateway.findAllById(List.of(1L, 2L))).thenReturn(List.of(entity, estoqueInsuficiente));

        var resultado = useCase.execute(List.of(suficiente, insuficiente)).itensAtualizados();

        assertAll(
                () -> assertEquals(7, entity.getQuantidade()),
                () -> assertEquals(StatusItemNecessario.UTILIZADO, resultado.get(0).getStatus()),
                () -> assertEquals(2, estoqueInsuficiente.getQuantidade()),
                () -> assertEquals(StatusItemNecessario.PENDENTE, resultado.get(1).getStatus()),
                () -> assertEquals(MotivoPendenciaItem.ESTOQUE_INSUFICIENTE, resultado.get(1).getMotivoPendencia()));
        verify(gateway).saveAll(List.of(entity));
    }

    @Test
    void deveTratarListaNulaVaziaEItemInexistente() {
        var useCase = new VerificarDisponibilidadeEBaixarEstoqueUseCase(gateway);
        assertTrue(useCase.execute(null).itensAtualizados().isEmpty());
        assertTrue(useCase.execute(List.of()).itensAtualizados().isEmpty());

        when(gateway.findAllById(List.of(9L))).thenReturn(List.of());
        assertEquals(HttpStatus.NOT_FOUND,
                assertThrows(ResponseStatusException.class,
                        () -> useCase.execute(List.of(item(9L, 1)))).getStatusCode());
        verify(gateway, never()).saveAll(any());
    }

    private ItemNecessarioEntity item(Long id, int quantidade) {
        return ItemNecessarioEntity.criar(
                id, "Item", CategoriaPecaInsumo.PECA, BigDecimal.ONE, quantidade, null);
    }

    private PecaInsumoEntity estoque(Long id, int quantidade) {
        var estoque = new PecaInsumoEntity();
        estoque.setId(id);
        estoque.setNome("Item " + id);
        estoque.setTipo(CategoriaPecaInsumo.PECA);
        estoque.setValor(BigDecimal.ONE);
        estoque.setQuantidade(quantidade);
        return estoque;
    }
}
