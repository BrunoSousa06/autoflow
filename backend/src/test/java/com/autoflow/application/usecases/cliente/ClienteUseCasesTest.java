package com.autoflow.application.usecases.cliente;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.application.gateway.ClienteGateway;
import com.autoflow.infrastructure.persistence.mapper.ClienteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteUseCasesTest {

    @Mock
    private ClienteGateway clienteGateway;

    @Spy
    private ClienteMapper clienteMapper;

    @InjectMocks
    private CriarClienteUseCase criarClienteUseCase;

    @InjectMocks
    private BuscarClientePorEmailUseCase buscarClientePorEmailUseCase;

    @InjectMocks
    private BuscarClientePorIdUseCase buscarClientePorIdUseCase;

    @InjectMocks
    private ListarTodosClientesUseCase listarTodosClientesUseCase;

    @InjectMocks
    private AtualizarClienteUseCase atualizarClienteUseCase;

    @InjectMocks
    private DeletarClienteUseCase deletarClienteUseCase;

    @InjectMocks
    private ListarClienteUseCase listarClienteUseCase;

    private ClienteInput clienteInput;
    private ClienteEntity clienteEntity;
    private ClienteOutput clienteOutput;

    @BeforeEach
    void setup() {
        clienteInput = new ClienteInput(
                "Bruno",
                "12345678901",
                "11999999999",
                "bruno@email.com"
        );

        clienteEntity = new ClienteEntity();
        clienteEntity.setId(1L);
        clienteEntity.setNome("Bruno");
        clienteEntity.setCpfCnpj("12345678901");
        clienteEntity.setTelefone("11999999999");
        clienteEntity.setEmail("bruno@email.com");

        clienteOutput = ClienteOutput.builder()
                .id(1L)
                .nome("Bruno")
                .cpfCnpj("12345678901")
                .telefone("11999999999")
                .email("bruno@email.com")
                .build();
    }

    @Nested
    class CriarClienteUseCaseTests {

        @Test
        void deveCriarClienteComSucesso() {
            when(clienteGateway.existsByCpfCnpj("12345678901")).thenReturn(false);
            when(clienteMapper.mapToEntity(any(ClienteInput.class))).thenReturn(clienteEntity);
            when(clienteGateway.save(any(ClienteEntity.class))).thenReturn(clienteEntity);
            when(clienteMapper.mapToOutput(clienteEntity)).thenReturn(clienteOutput);

            ClienteOutput resultado = criarClienteUseCase.execute(clienteInput);

            assertNotNull(resultado);
            assertEquals("Bruno", resultado.nome());
            assertEquals("12345678901", resultado.cpfCnpj());
            
            verify(clienteGateway).existsByCpfCnpj("12345678901");
            verify(clienteMapper).mapToEntity(any(ClienteInput.class));
            verify(clienteGateway).save(any(ClienteEntity.class));
            verify(clienteMapper).mapToOutput(clienteEntity);
        }

        @Test
        void deveLancarConflictQuandoCpfCnpjJaExistir() {
            when(clienteGateway.existsByCpfCnpj("12345678901")).thenReturn(true);

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> criarClienteUseCase.execute(clienteInput)
            );

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            verify(clienteGateway).existsByCpfCnpj("12345678901");
            verify(clienteGateway, never()).save(any());
        }
    }

    @Nested
    class BuscarPorEmailUseCaseTests {

        @Test
        void deveBuscarClientePorEmailComSucesso() {
            when(clienteGateway.findByUsuarioEmail("bruno@email.com")).thenReturn(Optional.of(clienteEntity));
            when(clienteMapper.mapToOutput(clienteEntity)).thenReturn(clienteOutput);
            ClienteOutput resultado = buscarClientePorEmailUseCase.execute("bruno@email.com");

            assertNotNull(resultado);
            assertEquals("Bruno", resultado.nome());
            verify(clienteGateway).findByUsuarioEmail("bruno@email.com");
        }

        @Test
        void deveLancarNotFoundQuandoEmailNaoExistir() {
            when(clienteGateway.findByUsuarioEmail("naoexiste@email.com")).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> buscarClientePorEmailUseCase.execute("naoexiste@email.com")
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(clienteGateway).findByUsuarioEmail("naoexiste@email.com");
        }
    }

    @Nested
    class BuscarPorIdUseCaseTests {

        @Test
        void deveBuscarClientePorIdComSucesso() {
            when(clienteGateway.findById(1L)).thenReturn(Optional.of(clienteEntity));

            ClienteEntity resultado = buscarClientePorIdUseCase.execute(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            verify(clienteGateway).findById(1L);
        }

        @Test
        void deveLancarNotFoundQuandoIdNaoExistir() {
            when(clienteGateway.findById(999L)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> buscarClientePorIdUseCase.execute(999L)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(clienteGateway).findById(999L);
        }
    }

    @Nested
    class ListarTodosUseCaseTests {

        @Test
        void deveListarTodosClientesComSucesso() {
            List<ClienteEntity> entities = List.of(clienteEntity);
            List<ClienteOutput> outputs = List.of(clienteOutput);

            when(clienteGateway.findAll()).thenReturn(entities);
            when(clienteMapper.mapToListOutput(entities)).thenReturn(outputs);
            List<ClienteOutput> resultado = listarTodosClientesUseCase.execute();

            assertNotNull(resultado);
            assertEquals(1, resultado.size());
            verify(clienteGateway).findAll();
        }

        @Test
        void deveRetornarListaVaziaSemClientes() {
            when(clienteGateway.findAll()).thenReturn(List.of());

            List<ClienteOutput> resultado = listarTodosClientesUseCase.execute();

            assertNotNull(resultado);
            assertTrue(resultado.isEmpty());
            verify(clienteGateway).findAll();
        }
    }

    @Nested
    class AtualizarUseCaseTests {

        @Test
        void deveAtualizarClienteComSucesso() {
            when(clienteGateway.findById(1L)).thenReturn(Optional.of(clienteEntity));
            Mockito.doNothing().when(clienteMapper).updateEntity(any(ClienteInput.class), eq(clienteEntity));
            when(clienteGateway.save(any(ClienteEntity.class))).thenReturn(clienteEntity);
            when(clienteMapper.mapToOutput(clienteEntity)).thenReturn(clienteOutput);

            ClienteOutput resultado = atualizarClienteUseCase.execute(1L, clienteInput);

            assertNotNull(resultado);
            assertEquals("Bruno", resultado.nome());
            verify(clienteGateway).findById(1L);
            verify(clienteMapper).updateEntity(any(ClienteInput.class), eq(clienteEntity));
            verify(clienteGateway).save(any(ClienteEntity.class));
        }

        @Test
        void deveLancarNotFoundQuandoClienteNaoExistir() {
            when(clienteGateway.findById(999L)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> atualizarClienteUseCase.execute(999L, clienteInput)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(clienteGateway).findById(999L);
            verify(clienteGateway, never()).save(any());
        }
    }

    @Nested
    class DeletarUseCaseTests {

        @Test
        void deveDeletarClienteComSucesso() {
            when(clienteGateway.existsById(1L)).thenReturn(true);
            doNothing().when(clienteGateway).deleteById(1L);

            assertDoesNotThrow(() -> deletarClienteUseCase.execute(1L));

            verify(clienteGateway).existsById(1L);
            verify(clienteGateway).deleteById(1L);
        }

        @Test
        void deveLancarNotFoundQuandoClienteNaoExistir() {
            when(clienteGateway.existsById(999L)).thenReturn(false);

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> deletarClienteUseCase.execute(999L)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            verify(clienteGateway).existsById(999L);
            verify(clienteGateway, never()).deleteById(any());
        }
    }

    @Nested
    class ListarUseCaseTests {

        @Test
        void deveBuscarPorCpfQuandoDocumentoPossuir11Digitos() {
            when(clienteGateway.findByCpfCnpj("12345678901")).thenReturn(Optional.of(clienteEntity));
            when(clienteMapper.mapToOutput(clienteEntity)).thenReturn(clienteOutput);

            ClienteOutput resultado = listarClienteUseCase.execute(12345678901L);

            assertNotNull(resultado);
            verify(clienteGateway).findByCpfCnpj("12345678901");
        }

        @Test
        void deveBuscarPorIdQuandoDocumentoNaoForCpfOuCnpj() {
            when(clienteGateway.findById(1L)).thenReturn(Optional.of(clienteEntity));
            when(clienteMapper.mapToOutput(clienteEntity)).thenReturn(clienteOutput);

            ClienteOutput resultado = listarClienteUseCase.execute(1L);

            assertNotNull(resultado);
            verify(clienteGateway).findById(1L);
        }

        @Test
        void deveBuscarPorCnpjQuandoDocumentoPossuir14Digitos() {
            when(clienteGateway.findByCpfCnpj("12345678901234")).thenReturn(Optional.of(clienteEntity));
            when(clienteMapper.mapToOutput(clienteEntity)).thenReturn(clienteOutput);

            ClienteOutput resultado = listarClienteUseCase.execute(12345678901234L);

            assertNotNull(resultado);
            verify(clienteGateway).findByCpfCnpj("12345678901234");
        }

        @Test
        void deveLancarNotFoundQuandoClienteNaoExistir() {
            when(clienteGateway.findById(999L)).thenReturn(Optional.empty());
            // Removed unnecessary stubbing: when(clienteGateway.findByCpfCnpj(anyString())).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> listarClienteUseCase.execute(999L)
            );

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        }
    }
}