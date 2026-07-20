package com.autoflow.application.usecases.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.infrastructure.persistence.mapper.ServicoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for all Servico use cases.
 * Tests both success and failure paths for CRUD operations and metrics calculation.
 */
@ExtendWith(MockitoExtension.class)
class ServicoUseCasesTest {

    @Mock
    private ServicoGateway servicoGateway;

    @Mock
    private ServicoMapper servicoMapper;

    private ServicoEntity servicoEntity;
    private ServicoOutput servicoOutput;
    private ServicoInput servicoInput;

    @BeforeEach
    void setup() {
        servicoEntity = new ServicoEntity();
        servicoEntity.setId(1L);
        servicoEntity.setNome("Revisão Completa");
        servicoEntity.setDescricao("Revisão completa do veículo");
        servicoEntity.setValor(new BigDecimal("150.00"));
        servicoEntity.setAtivo(true);

        servicoOutput = ServicoOutput.builder()
                .id(1L)
                .nome("Revisão Completa")
                .descricao("Revisão completa do veículo")
                .valor(new BigDecimal("150.00"))
                .ativo(true)
                .build();

        servicoInput = new ServicoInput(
                "Revisão Completa",
                "Revisão completa do veículo",
                new BigDecimal("150.00")
        );
    }

    @Nested
    class CriarServicoUseCaseTests {

        private CriarServicoUseCase criarServicoUseCase;

        @BeforeEach
        void setup() {
            criarServicoUseCase = new CriarServicoUseCase(servicoGateway, servicoMapper);
        }

        @Test
        void deveCriarServicoComSucesso() {
            when(servicoGateway.findByNomeIgnoreCase("Revisão Completa"))
                    .thenReturn(Optional.empty());
            when(servicoGateway.save(any(ServicoEntity.class)))
                    .thenReturn(servicoEntity);
            when(servicoMapper.mapToOutput(servicoEntity))
                    .thenReturn(servicoOutput);

            ServicoOutput resultado = criarServicoUseCase.execute(servicoInput);

            assertNotNull(resultado);
            assertEquals("Revisão Completa", resultado.getNome());
            assertEquals(new BigDecimal("150.00"), resultado.getValor());
            assertTrue(resultado.isAtivo());

            verify(servicoGateway).findByNomeIgnoreCase("Revisão Completa");
            verify(servicoGateway).save(any(ServicoEntity.class));
            verify(servicoMapper).mapToOutput(servicoEntity);
        }

        @Test
        void deveLancarConflictQuandoNomeJaExistir() {
            when(servicoGateway.findByNomeIgnoreCase("Revisão Completa"))
                    .thenReturn(Optional.of(servicoEntity));

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> criarServicoUseCase.execute(servicoInput)
            );

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("Serviço já foi cadastrado", exception.getReason());

            verify(servicoGateway).findByNomeIgnoreCase("Revisão Completa");
            verify(servicoGateway, never()).save(any());
        }
    }

    @Nested
    class BuscarServicoPorIdUseCaseTests {

        private BuscarServicoPorIdUseCase buscarServicoPorIdUseCase;

        @BeforeEach
        void setup() {
            buscarServicoPorIdUseCase = new BuscarServicoPorIdUseCase(servicoGateway, servicoMapper);
        }

        @Test
        void deveBuscarServicoPorIdComSucesso() {
            when(servicoGateway.findById(1L))
                    .thenReturn(Optional.of(servicoEntity));
            when(servicoMapper.mapToOutput(servicoEntity))
                    .thenReturn(servicoOutput);

            ServicoOutput resultado = buscarServicoPorIdUseCase.execute(1L);

            assertNotNull(resultado);
            assertEquals(servicoOutput, resultado);
            verify(servicoGateway).findById(1L);
            verify(servicoMapper).mapToOutput(servicoEntity);
        }

        @Test
        void deveLancarNotFoundQuandoIdNaoExistir() {
            when(servicoGateway.findById(1L))
                    .thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> buscarServicoPorIdUseCase.execute(1L)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(servicoGateway).findById(1L);
            verifyNoInteractions(servicoMapper);
        }
    }

    @Nested
    class ListarServicosUseCaseTests {

        private ListarServicosUseCase listarServicosUseCase;

        @BeforeEach
        void setup() {
            listarServicosUseCase = new ListarServicosUseCase(servicoGateway, servicoMapper);
        }

        @Test
        void deveListarServicosComSucesso() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ServicoEntity> entityPage = new PageImpl<>(List.of(servicoEntity));

            when(servicoGateway.findAllByAtivoTrue(pageable))
                    .thenReturn(entityPage);
            when(servicoMapper.mapToOutput(servicoEntity))
                    .thenReturn(servicoOutput);

            Page<ServicoOutput> resultado = listarServicosUseCase.execute(pageable);

            assertNotNull(resultado);
            assertEquals(1, resultado.getContent().size());
            assertEquals(servicoOutput, resultado.getContent().get(0));

            verify(servicoGateway).findAllByAtivoTrue(pageable);
            verify(servicoMapper).mapToOutput(servicoEntity);
        }

        @Test
        void deveRetornarPaginaVaziaQuandoNaoHaServicos() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<ServicoEntity> emptyPage = new PageImpl<>(List.of());

            when(servicoGateway.findAllByAtivoTrue(pageable))
                    .thenReturn(emptyPage);

            Page<ServicoOutput> resultado = listarServicosUseCase.execute(pageable);

            assertNotNull(resultado);
            assertEquals(0, resultado.getContent().size());

            verify(servicoGateway).findAllByAtivoTrue(pageable);
            verifyNoInteractions(servicoMapper);
        }
    }

    @Nested
    class AtualizarServicoUseCaseTests {

        private AtualizarServicoUseCase atualizarServicoUseCase;

        @BeforeEach
        void setup() {
            atualizarServicoUseCase = new AtualizarServicoUseCase(servicoGateway, servicoMapper);
        }

        @Test
        void deveAtualizarServicoComSucesso() {
            ServicoInput novoInput = new ServicoInput(
                    "Revisão Básica",
                    "Revisão básica do veículo",
                    new BigDecimal("100.00")
            );

            when(servicoGateway.findById(1L))
                    .thenReturn(Optional.of(servicoEntity));
            when(servicoGateway.save(any(ServicoEntity.class)))
                    .thenReturn(servicoEntity);
            when(servicoMapper.mapToOutput(servicoEntity))
                    .thenReturn(servicoOutput);

            ServicoOutput resultado = atualizarServicoUseCase.execute(1L, novoInput);

            assertNotNull(resultado);
            assertEquals(servicoOutput, resultado);

            verify(servicoGateway).findById(1L);
            verify(servicoGateway).save(servicoEntity);
            verify(servicoMapper).mapToOutput(servicoEntity);
        }

        @Test
        void deveLancarNotFoundAoAtualizarServicoInexistente() {
            when(servicoGateway.findById(1L))
                    .thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> atualizarServicoUseCase.execute(1L, servicoInput)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(servicoGateway).findById(1L);
            verify(servicoGateway, never()).save(any());
        }
    }

    @Nested
    class InativarServicoUseCaseTests {

        private InativarServicoUseCase inativarServicoUseCase;

        @BeforeEach
        void setup() {
            inativarServicoUseCase = new InativarServicoUseCase(servicoGateway);
        }

        @Test
        void deveInativarServicoComSucesso() {
            when(servicoGateway.findById(1L))
                    .thenReturn(Optional.of(servicoEntity));
            when(servicoGateway.save(any(ServicoEntity.class)))
                    .thenReturn(servicoEntity);

            inativarServicoUseCase.execute(1L);

            assertTrue(!servicoEntity.isAtivo());
            verify(servicoGateway).findById(1L);
            verify(servicoGateway).save(servicoEntity);
        }

        @Test
        void deveLancarNotFoundAoInativarServicoInexistente() {
            when(servicoGateway.findById(1L))
                    .thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> inativarServicoUseCase.execute(1L)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(servicoGateway).findById(1L);
            verify(servicoGateway, never()).save(any());
        }
    }

}
