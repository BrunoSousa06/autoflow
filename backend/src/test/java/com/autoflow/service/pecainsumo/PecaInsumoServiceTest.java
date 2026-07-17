package com.autoflow.service.pecainsumo;

import com.autoflow.controller.pecainsumo.request.PecaInsumoRequest;
import com.autoflow.controller.pecainsumo.response.PecaInsumoResponse;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.MotivoPendenciaItem;
import com.autoflow.domain.ordemservico.StatusItemNecessario;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.infrastructure.persistence.mapper.PecaInsumoMapper;
import com.autoflow.repository.pecainsumo.PecaInsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    class ListarPaginadoTests {

        @Test
        void deveListarPaginadoSemFiltros() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PecaInsumoEntity> pageEntidades = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageEntidades);
            when(mapper.toResponse(entity)).thenReturn(response);

            Page<PecaInsumoResponse> resultado = service.listarPaginado(pageable, null, null);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.getTotalElements()),
                    () -> assertEquals(1, resultado.getContent().size()),
                    () -> assertEquals(response, resultado.getContent().getFirst())
            );

            verify(repository).findAll(any(Specification.class), eq(pageable));
            verify(mapper).toResponse(entity);
        }

        @Test
        void deveRetornarPaginaVaziaQuandoNaoHouverItens() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PecaInsumoEntity> pageVazia = new PageImpl<>(List.of(), pageable, 0);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageVazia);

            Page<PecaInsumoResponse> resultado = service.listarPaginado(pageable, null, null);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(0, resultado.getTotalElements()),
                    () -> assertTrue(resultado.getContent().isEmpty())
            );

            verify(repository).findAll(any(Specification.class), eq(pageable));
            verify(mapper, never()).toResponse(any());
        }

        @Test
        void deveRespeitarParametrosDePaginacao() {
            Pageable pageable = PageRequest.of(1, 5);
            Page<PecaInsumoEntity> page = new PageImpl<>(List.of(entity), pageable, 6);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(mapper.toResponse(entity)).thenReturn(response);

            Page<PecaInsumoResponse> resultado = service.listarPaginado(pageable, null, null);

            assertAll(
                    () -> assertEquals(1, resultado.getNumber()),
                    () -> assertEquals(5, resultado.getSize()),
                    () -> assertEquals(6, resultado.getTotalElements()),
                    () -> assertEquals(2, resultado.getTotalPages())
            );
        }

        @Test
        void deveListarFiltrandoPorNome() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PecaInsumoEntity> pageEntidades = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageEntidades);
            when(mapper.toResponse(entity)).thenReturn(response);

            Page<PecaInsumoResponse> resultado = service.listarPaginado(pageable, "filtro", null);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.getTotalElements())
            );

            verify(repository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        void deveListarFiltrandoPorTipo() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<PecaInsumoEntity> pageEntidades = new PageImpl<>(List.of(entity), pageable, 1);

            when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(pageEntidades);
            when(mapper.toResponse(entity)).thenReturn(response);

            Page<PecaInsumoResponse> resultado = service.listarPaginado(pageable, null, CategoriaPecaInsumo.PECA);

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.getTotalElements())
            );

            verify(repository).findAll(any(Specification.class), eq(pageable));
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

    @Nested
    class VerificarDisponibilidadeEBaixarTests {

        @Test
        void deveMarcarDisponivelEBaixarEstoqueQuandoHouverQuantidade() {
            PecaInsumoEntity filtro = criarPecaInsumo(
                    1L,
                    "Filtro de Oleo",
                    CategoriaPecaInsumo.PECA,
                    new BigDecimal("50.00"),
                    10
            );
            List<ItemNecessarioEntity> itens = List.of(criarItemNecessarioSolicitado(1L, 2));

            when(repository.findAllById(List.of(1L)))
                    .thenReturn(List.of(filtro));

            BaixaEstoqueResult resultado = service.verificarDisponibilidadeEBaixar(itens);

            ItemNecessarioEntity itemAtualizado = resultado.itensAtualizados().getFirst();
            assertAll(
                    () -> assertEquals(8, filtro.getQuantidade()),
                    () -> assertEquals(1L, itemAtualizado.getPecaInsumoId()),
                    () -> assertEquals("Filtro de Oleo", itemAtualizado.getNome()),
                    () -> assertEquals(CategoriaPecaInsumo.PECA, itemAtualizado.getTipo()),
                    () -> assertEquals(new BigDecimal("50.00"), itemAtualizado.getValorUnitario()),
                    () -> assertEquals(2, itemAtualizado.getQuantidade()),
                    () -> assertEquals(new BigDecimal("100.00"), itemAtualizado.getValorTotal()),
                    () -> assertEquals(StatusItemNecessario.UTILIZADO, itemAtualizado.getStatus()),
                    () -> assertEquals(8, itemAtualizado.getQuantidadeDisponivel()),
                    () -> assertNull(itemAtualizado.getMotivoPendencia()),
                    () -> assertNull(itemAtualizado.getMensagemStatus())
            );
            verify(repository).findAllById(List.of(1L));
            verify(repository).saveAll(List.of(filtro));
        }

        @Test
        void deveMarcarPendenteENaoBaixarEstoqueQuandoNaoHouverQuantidade() {
            PecaInsumoEntity filtro = criarPecaInsumo(
                    1L,
                    "Filtro de Oleo",
                    CategoriaPecaInsumo.PECA,
                    new BigDecimal("50.00"),
                    2
            );
            List<ItemNecessarioEntity> itens = List.of(criarItemNecessarioSolicitado(1L, 3));

            when(repository.findAllById(List.of(1L)))
                    .thenReturn(List.of(filtro));

            BaixaEstoqueResult resultado = service.verificarDisponibilidadeEBaixar(itens);

            ItemNecessarioEntity itemAtualizado = resultado.itensAtualizados().getFirst();
            assertAll(
                    () -> assertEquals(2, filtro.getQuantidade()),
                    () -> assertEquals(1L, itemAtualizado.getPecaInsumoId()),
                    () -> assertEquals("Filtro de Oleo", itemAtualizado.getNome()),
                    () -> assertEquals(3, itemAtualizado.getQuantidade()),
                    () -> assertEquals(StatusItemNecessario.PENDENTE, itemAtualizado.getStatus()),
                    () -> assertEquals(2, itemAtualizado.getQuantidadeDisponivel()),
                    () -> assertEquals(MotivoPendenciaItem.ESTOQUE_INSUFICIENTE, itemAtualizado.getMotivoPendencia()),
                    () -> assertEquals(
                            "Estoque insuficiente. Solicitado: 3, disponivel: 2.",
                            itemAtualizado.getMensagemStatus()
                    )
            );
            verify(repository).findAllById(List.of(1L));
            verify(repository).saveAll(List.of());
        }

        @Test
        void deveLancarNotFoundQuandoItemNaoExistirNoEstoque() {
            List<ItemNecessarioEntity> itens = List.of(criarItemNecessarioSolicitado(1L, 3));

            when(repository.findAllById(List.of(1L)))
                    .thenReturn(List.of());

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.verificarDisponibilidadeEBaixar(itens)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(repository).findAllById(List.of(1L));
            verify(repository, never()).saveAll(any());
        }

        @Test
        void deveRetornarResultadoVazioQuandoListaForVazia() {
            BaixaEstoqueResult result = service.verificarDisponibilidadeEBaixar(Collections.emptyList());

            assertNotNull(result);
            assertTrue(result.itensAtualizados().isEmpty());
        }
    }

    private PecaInsumoEntity criarPecaInsumo(
            Long id,
            String nome,
            CategoriaPecaInsumo tipo,
            BigDecimal valor,
            int quantidade
    ) {
        PecaInsumoEntity pecaInsumo = new PecaInsumoEntity();
        pecaInsumo.setId(id);
        pecaInsumo.setNome(nome);
        pecaInsumo.setTipo(tipo);
        pecaInsumo.setValor(valor);
        pecaInsumo.setQuantidade(quantidade);
        return pecaInsumo;
    }

    private ItemNecessarioEntity criarItemNecessarioSolicitado(Long pecaInsumoId, int quantidade) {
        return ItemNecessarioEntity.criar(
                pecaInsumoId,
                "Item solicitado",
                CategoriaPecaInsumo.PECA,
                BigDecimal.ZERO,
                quantidade,
                null
        );
    }
}
