package com.autoflow.service.cliente;

import com.autoflow.controller.cliente.request.ClienteRequest;
import com.autoflow.controller.cliente.response.ClienteResponse;
import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.mapper.ClienteMapper;
import com.autoflow.repository.cliente.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @InjectMocks
    private ClienteService clienteService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    private ClienteEntity clienteEntity;
    private ClienteResponse clienteResponse;
    private ClienteRequest clienteRequest;

    @BeforeEach
    void setup() {

        clienteEntity = new ClienteEntity();
        clienteEntity.setId(1L);
        clienteEntity.setCpfCnpj("12345678901");

        clienteResponse = new ClienteResponse(
                1L,
                "João",
                "12345678901",
                "11999999999", "bruno@hotmail.com", null
        );

        clienteRequest = new ClienteRequest(
                "João Atualizado",
                "12345678901",
                "1188888888", "bruno@hotmail.com"
        );
    }

    @Test
    void deveCadastrarCliente() {
        ClienteRequest request = new ClienteRequest(
                "Bruno",
                "12345678901",
                "11999999999",
                "joao@email.com"
        );

        ClienteEntity entity = new ClienteEntity();
        entity.setId(1L);
        entity.setNome("Bruno");
        entity.setCpfCnpj("12345678901");

        ClienteResponse response = new ClienteResponse(
                1L,
                "Bruno",
                "12345678901",
                "11999999999",
                "joao@email.com",
                null
        );

        when(clienteRepository.existsByCpfCnpj("12345678901"))
                .thenReturn(false);

        when(clienteMapper.mapToEntity(request))
                .thenReturn(entity);

        when(clienteRepository.save(entity))
                .thenReturn(entity);

        when(clienteMapper.maptoResponse(entity))
                .thenReturn(response);

        ClienteResponse resultado = clienteService.cadastrar(request);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Bruno", resultado.nome());
        assertEquals("12345678901", resultado.cpfCnpj());

        verify(clienteRepository).existsByCpfCnpj("12345678901");
        verify(clienteMapper).mapToEntity(request);
        verify(clienteRepository).save(entity);
        verify(clienteMapper).maptoResponse(entity);
    }

    @Test
    void deveLancarConflictQuandoCpfCnpjJaExistir() {
        ClienteRequest request = new ClienteRequest(
                "Bruno",
                "12345678901",
                "11999999999",
                "joao@email.com"
        );

        when(clienteRepository.existsByCpfCnpj("12345678901"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> clienteService.cadastrar(request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("CPF/CNPJ ja cadastrado", exception.getReason());

        verify(clienteRepository).existsByCpfCnpj("12345678901");
        verify(clienteRepository, never()).save(any());
        verify(clienteMapper, never()).mapToEntity(any());
        verify(clienteMapper, never()).maptoResponse(any());
    }

    @Nested
    class BuscarPorIdTests {

        @Test
        void deveBuscarClientePorId() {

            when(clienteRepository.findById(1L))
                    .thenReturn(Optional.of(clienteEntity));

            ClienteEntity resultado =
                    clienteService.buscarPorId(1L);

            assertEquals(clienteEntity, resultado);

            verify(clienteRepository).findById(1L);
        }

        @Test
        void deveLancarExcecaoQuandoIdNaoExistir() {

            when(clienteRepository.findById(1L))
                    .thenReturn(Optional.empty());

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> clienteService.buscarPorId(1L)
                    );

            assertEquals(
                    HttpStatus.NOT_FOUND,
                    exception.getStatusCode()
            );

            verify(clienteRepository).findById(1L);
        }
    }

    @Nested
    class BuscarPorCpfCnpjTests {

        @Test
        void deveBuscarClientePorCpfCnpj() {

            when(clienteRepository.findByCpfCnpj("12345678901"))
                    .thenReturn(Optional.of(clienteEntity));

            ClienteEntity resultado =
                    clienteService.buscarPorCpfCnpj("12345678901");

            assertEquals(clienteEntity, resultado);

            verify(clienteRepository)
                    .findByCpfCnpj("12345678901");

            verifyNoInteractions(clienteMapper);
        }

        @Test
        void deveLancarExcecaoQuandoCpfNaoExistir() {

            when(clienteRepository.findByCpfCnpj("12345678901"))
                    .thenReturn(Optional.empty());

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> clienteService.buscarPorCpfCnpj("12345678901")
                    );

            assertEquals(
                    HttpStatus.NOT_FOUND,
                    exception.getStatusCode()
            );

            verify(clienteRepository)
                    .findByCpfCnpj("12345678901");
        }
    }

    @Nested
    class ListarTests {

        @Test
        void deveBuscarPorCpfQuandoDocumentoPossuir11Digitos() {

            when(clienteRepository.findByCpfCnpj("12345678901"))
                    .thenReturn(Optional.of(clienteEntity));

            when(clienteMapper.maptoResponse(clienteEntity))
                    .thenReturn(clienteResponse);

            ClienteResponse resultado =
                    clienteService.listar(12345678901L);

            assertEquals(clienteResponse, resultado);

            verify(clienteRepository)
                    .findByCpfCnpj("12345678901");
        }

        @Test
        void deveBuscarPorIdQuandoDocumentoNaoForCpfOuCnpj() {

            when(clienteRepository.findById(1L))
                    .thenReturn(Optional.of(clienteEntity));

            when(clienteMapper.maptoResponse(clienteEntity))
                    .thenReturn(clienteResponse);

            ClienteResponse resultado =
                    clienteService.listar(1L);

            assertEquals(clienteResponse, resultado);

            verify(clienteRepository).findById(1L);
            verify(clienteMapper).maptoResponse(clienteEntity);
        }

        @Test
        void deveLancarExcecaoQuandoIdNaoExistir() {

            when(clienteRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResponseStatusException.class,
                    () -> clienteService.listar(1L)
            );

            verify(clienteRepository).findById(1L);
        }
    }

    @Nested
    class ListarTodosTests {

        @Test
        void deveListarTodosClientes() {

            List<ClienteEntity> entities =
                    List.of(clienteEntity);

            List<ClienteResponse> responses =
                    List.of(clienteResponse);

            when(clienteRepository.findAll())
                    .thenReturn(entities);

            when(clienteMapper.mapToList(entities))
                    .thenReturn(responses);

            List<ClienteResponse> resultado =
                    clienteService.listarTodosClientes();

            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(1, resultado.size())
            );

            verify(clienteRepository).findAll();
            verify(clienteMapper).mapToList(entities);
        }
    }

    @Nested
    class AtualizarTests {

        @Test
        void deveAtualizarCliente() {

            UsuarioEntity usuario = new UsuarioEntity();
            usuario.setNome("Nome Antigo");
            usuario.setEmail("antigo@email.com");

            clienteEntity.setUsuario(usuario);

            when(clienteRepository.findById(1L))
                    .thenReturn(Optional.of(clienteEntity));

            when(clienteRepository.save(clienteEntity))
                    .thenReturn(clienteEntity);

            when(clienteMapper.maptoResponse(clienteEntity))
                    .thenReturn(clienteResponse);

            ClienteResponse resultado =
                    clienteService.atualizar(clienteRequest, 1L);

            assertEquals(clienteResponse, resultado);

            assertEquals(clienteRequest.nome(), usuario.getNome());
            assertEquals(clienteRequest.email(), usuario.getEmail());

            verify(clienteRepository).findById(1L);

            verify(clienteMapper)
                    .updateEntity(clienteRequest, clienteEntity);

            verify(clienteRepository)
                    .save(clienteEntity);

            verify(clienteMapper)
                    .maptoResponse(clienteEntity);
        }

        @Test
        void deveLancarExcecaoAoAtualizarClienteInexistente() {

            when(clienteRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResponseStatusException.class,
                    () -> clienteService.atualizar(clienteRequest, 1L)
            );

            verify(clienteRepository).findById(1L);

            verify(clienteRepository, never())
                    .save(any());
        }
    }

    @Nested
    class DeletarTests {

        @Test
        void deveDeletarCliente() {

            when(clienteRepository.existsById(1L))
                    .thenReturn(true);

            clienteService.deletar(1L);

            verify(clienteRepository).existsById(1L);
            verify(clienteRepository).deleteById(1L);
        }

        @Test
        void deveLancarExcecaoQuandoClienteNaoExistir() {

            when(clienteRepository.existsById(1L))
                    .thenReturn(false);

            ResponseStatusException exception =
                    assertThrows(
                            ResponseStatusException.class,
                            () -> clienteService.deletar(1L)
                    );

            assertEquals(
                    HttpStatus.NOT_FOUND,
                    exception.getStatusCode()
            );

            verify(clienteRepository).existsById(1L);

            verify(clienteRepository, never())
                    .deleteById(anyLong());
        }

        @Test
        void deveAtualizarClienteSemUsuarioAssociado() {

            clienteEntity.setUsuario(null);

            when(clienteRepository.findById(1L))
                    .thenReturn(Optional.of(clienteEntity));

            when(clienteRepository.save(clienteEntity))
                    .thenReturn(clienteEntity);

            when(clienteMapper.maptoResponse(clienteEntity))
                    .thenReturn(clienteResponse);

            ClienteResponse resultado =
                    clienteService.atualizar(clienteRequest, 1L);

            assertEquals(clienteResponse, resultado);

            verify(clienteRepository).findById(1L);
            verify(clienteMapper).updateEntity(clienteRequest, clienteEntity);
            verify(clienteRepository).save(clienteEntity);
            verify(clienteMapper).maptoResponse(clienteEntity);
        }
    }
}
