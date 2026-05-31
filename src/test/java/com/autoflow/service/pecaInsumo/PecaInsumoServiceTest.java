package com.autoflow.service.pecaInsumo;

import com.autoflow.controller.pecaInsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecaInsumo.response.PecaInsumoResponse;
import com.autoflow.domain.pecaInsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecaInsumo.PecaInsumoEntity;
import com.autoflow.mapper.PecaInsumoMapper;
import com.autoflow.repository.PecaInsumo.PecaInsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecaInsumoServiceTest {

    @InjectMocks
    private PecaInsumoService service;

    @Mock
    private PecaInsumoRepository repository;

    @Mock
    private PecaInsumoMapper mapper;

    private PecaInsumoRequest request;
    private PecaInsumoEntity entity;
    private PecaInsumoResponse response;

    @BeforeEach
    void setup() {

        request = new PecaInsumoRequest(
                "Filtro de Oleo", BigDecimal.valueOf(50.00),2, CategoriaPecaInsumo.PECA
        );

        entity = new PecaInsumoEntity();
        entity.setId(1L);
        entity.setNome("Filtro de Oleo");
        entity.setTipo(CategoriaPecaInsumo.PECA);

        response = new PecaInsumoResponse(
                1L,
                "Filtro de Oleo", BigDecimal.valueOf(50.00),2, CategoriaPecaInsumo.PECA
        );
    }

    @Nested
    class CadastrarTests {

        @Test
        void deveCadastrarComSucesso() {

            when(repository.findByNomeIgnoreCase(request.nome()))
                    .thenReturn(Optional.empty());

            when(mapper.mapToEntity(request))
                    .thenReturn(entity);

            when(repository.save(entity))
                    .thenReturn(entity);

            when(mapper.toResponse(entity))
                    .thenReturn(response);

            PecaInsumoResponse resultado =
                    service.cadastrar(request);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(response, resultado)
            );

            verify(repository)
                    .findByNomeIgnoreCase(request.nome());

            verify(mapper)
                    .mapToEntity(request);

            verify(repository)
                    .save(entity);

            verify(mapper)
                    .toResponse(entity);
        }

        @Test
        void deveLancarExcecaoQuandoPecaJaExistir() {

            when(repository.findByNomeIgnoreCase(request.nome()))
                    .thenReturn(Optional.of(entity));

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> service.cadastrar(request)
                    );

            assertAll(
                    () -> assertEquals(
                            HttpStatus.BAD_REQUEST,
                            exception.getStatusCode()
                    ),
                    () -> assertEquals(
                            "Peça/Insumo já foi cadastrado",
                            exception.getReason()
                    )
            );

            verify(repository)
                    .findByNomeIgnoreCase(request.nome());

            verify(repository, never())
                    .save(any());

            verify(mapper, never())
                    .mapToEntity(any());
        }
    }

    @Nested
    class ListarTests {

        @Test
        void deveListarTodasAsPecas() {

            List<PecaInsumoEntity> entities =
                    List.of(entity);

            List<PecaInsumoResponse> responses =
                    List.of(response);

            when(repository.findAll())
                    .thenReturn(entities);

            when(mapper.toResponseList(entities))
                    .thenReturn(responses);

            List<PecaInsumoResponse> resultado =
                    service.listar();

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.size()),
                    () -> assertEquals(response, resultado.getFirst())
            );

            verify(repository).findAll();
            verify(mapper).toResponseList(entities);
        }
    }

    @Nested
    class BuscarPorIdTests {

        @Test
        void deveBuscarPorIdComSucesso() {

            when(repository.findById(1L))
                    .thenReturn(Optional.of(entity));

            when(mapper.toResponse(entity))
                    .thenReturn(response);

            PecaInsumoResponse resultado =
                    service.buscarPorId(1L);

            assertEquals(response, resultado);

            verify(repository).findById(1L);
            verify(mapper).toResponse(entity);
        }

        @Test
        void deveLancarExcecaoQuandoIdNaoExistir() {

            when(repository.findById(1L))
                    .thenReturn(Optional.empty());

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> service.buscarPorId(1L)
                    );

            assertAll(
                    () -> assertEquals(
                            HttpStatus.NOT_FOUND,
                            exception.getStatusCode()
                    ),
                    () -> assertEquals(
                            "Peça/Insumo não encontrado com o ID: 1",
                            exception.getReason()
                    )
            );

            verify(repository).findById(1L);

            verify(mapper, never())
                    .toResponse(any());
        }

        @Test
        void deveBuscarEntityPorIdComSucesso() {

            when(repository.findById(1L))
                    .thenReturn(Optional.of(entity));


            PecaInsumoEntity resultado =
                    service.buscarEntityPorId(1L);

            assertEquals(entity, resultado);

            verify(repository).findById(1L);
        }

    }

    @Nested
    class AtualizarTests {

        @Test
        void deveAtualizarComSucesso() {

            when(repository.findById(1L))
                    .thenReturn(Optional.of(entity));

            when(repository.save(entity))
                    .thenReturn(entity);

            when(mapper.toResponse(entity))
                    .thenReturn(response);

            PecaInsumoResponse resultado =
                    service.atualizar(request, 1L);

            assertEquals(response, resultado);

            verify(repository).findById(1L);

            verify(mapper)
                    .updateEntity(request, entity);

            verify(repository)
                    .save(entity);

            verify(mapper)
                    .toResponse(entity);
        }

        @Test
        void deveLancarExcecaoQuandoIdNaoExistir() {

            when(repository.findById(1L))
                    .thenReturn(Optional.empty());

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> service.atualizar(request, 1L)
                    );

            assertEquals(
                    HttpStatus.NOT_FOUND,
                    exception.getStatusCode()
            );

            verify(repository).findById(1L);

            verify(repository, never())
                    .save(any());

            verify(mapper, never())
                    .updateEntity(any(), any());
        }
    }

    @Nested
    class DeletarTests {

        @Test
        void deveDeletarComSucesso() {

            when(repository.existsById(1L))
                    .thenReturn(true);

            service.deletar(1L);

            verify(repository).existsById(1L);
            verify(repository).deleteById(1L);
        }

        @Test
        void deveLancarExcecaoQuandoIdNaoExistir() {

            when(repository.existsById(1L))
                    .thenReturn(false);

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> service.deletar(1L)
                    );

            assertAll(
                    () -> assertEquals(
                            HttpStatus.NOT_FOUND,
                            exception.getStatusCode()
                    ),
                    () -> assertEquals(
                            "Peça/Insumo não encontrado com o ID: 1",
                            exception.getReason()
                    )
            );

            verify(repository).existsById(1L);

            verify(repository, never())
                    .deleteById(anyLong());
        }
    }
}
