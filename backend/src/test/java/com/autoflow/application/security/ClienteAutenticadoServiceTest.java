package com.autoflow.application.security;

import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.application.gateway.ClienteGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteAutenticadoServiceTest {

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private ClienteGateway clienteRepository;

    @InjectMocks
    private ClienteAutenticadoService clienteAutenticadoService;

    private ClienteEntity cliente;

    @BeforeEach
    void setUp() {
        cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("Bruno");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("bruno@email.com");
        cliente.setTelefone("11999999999");
    }

    @Test
    void deveRetornarNullQuandoUsuarioNaoEhCliente() {

        when(usuarioAutenticadoService.isCliente()).thenReturn(false);

        ClienteEntity resultado = clienteAutenticadoService.getClienteLogado();

        assertNull(resultado);

        verify(usuarioAutenticadoService).isCliente();
        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deveRetornarClienteLogado() {

        when(usuarioAutenticadoService.isCliente()).thenReturn(true);
        when(usuarioAutenticadoService.getEmail()).thenReturn(cliente.getEmail());
        when(clienteRepository.findByUsuarioEmail(cliente.getEmail()))
                .thenReturn(Optional.of(cliente));

        ClienteEntity resultado = clienteAutenticadoService.getClienteLogado();

        assertNotNull(resultado);
        assertEquals(cliente.getId(), resultado.getId());
        assertEquals(cliente.getEmail(), resultado.getEmail());

        verify(usuarioAutenticadoService).isCliente();
        verify(usuarioAutenticadoService).getEmail();
        verify(clienteRepository).findByUsuarioEmail(cliente.getEmail());
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoEncontrado() {

        when(usuarioAutenticadoService.isCliente()).thenReturn(true);
        when(usuarioAutenticadoService.getEmail()).thenReturn("bruno@email.com");
        when(clienteRepository.findByUsuarioEmail("bruno@email.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> clienteAutenticadoService.getClienteLogado());

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals(
                "Cliente não encontrado para o usuário autenticado",
                exception.getReason());

        verify(usuarioAutenticadoService).isCliente();
        verify(usuarioAutenticadoService).getEmail();
        verify(clienteRepository).findByUsuarioEmail("bruno@email.com");
    }

    @Test
    void deveRetornarClienteId() {

        when(usuarioAutenticadoService.isCliente()).thenReturn(true);
        when(usuarioAutenticadoService.getEmail()).thenReturn(cliente.getEmail());
        when(clienteRepository.findByUsuarioEmail(cliente.getEmail()))
                .thenReturn(Optional.of(cliente));

        Long id = clienteAutenticadoService.getClienteId();

        assertEquals(cliente.getId(), id);

        verify(usuarioAutenticadoService).isCliente();
        verify(usuarioAutenticadoService).getEmail();
        verify(clienteRepository).findByUsuarioEmail(cliente.getEmail());
    }

    @Test
    void deveRetornarNullQuandoGetClienteIdEUsuarioNaoEhCliente() {

        when(usuarioAutenticadoService.isCliente()).thenReturn(false);

        Long id = clienteAutenticadoService.getClienteId();

        assertNull(id);

        verify(usuarioAutenticadoService).isCliente();
        verifyNoInteractions(clienteRepository);
    }
}
