package com.autoflow.application.security;

import com.autoflow.application.exception.ClienteAutenticadoNaoEncontradoException;
import com.autoflow.application.exception.UsuarioNaoAutenticadoException;
import com.autoflow.application.gateway.CurrentUserGateway;
import com.autoflow.application.gateway.VeiculoClienteGateway;
import com.autoflow.application.output.security.CurrentUser;
import com.autoflow.domain.usuario.RoleEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteAutenticadoServiceTest {

    @Mock
    private CurrentUserGateway currentUserGateway;
    @Mock
    private VeiculoClienteGateway clienteGateway;
    @InjectMocks
    private ClienteAutenticadoService service;

    @Test
    void deveRetornarVazioParaUsuarioQueNaoEhCliente() {
        when(currentUserGateway.getCurrentUser())
                .thenReturn(Optional.of(new CurrentUser("admin@test.com", RoleEnum.ADMIN)));

        assertEquals(Optional.empty(), service.getClienteId());
        verifyNoInteractions(clienteGateway);
    }

    @Test
    void deveRetornarIdDoClienteAutenticado() {
        when(currentUserGateway.getCurrentUser())
                .thenReturn(Optional.of(new CurrentUser("cliente@test.com", RoleEnum.CLIENTE)));
        when(clienteGateway.findIdByUsuarioEmail("cliente@test.com"))
                .thenReturn(Optional.of(10L));

        assertEquals(Optional.of(10L), service.getClienteId());
        verify(clienteGateway).findIdByUsuarioEmail("cliente@test.com");
    }

    @Test
    void deveRetornar403QuandoClienteNaoEstiverVinculado() {
        when(currentUserGateway.getCurrentUser())
                .thenReturn(Optional.of(new CurrentUser("cliente@test.com", RoleEnum.CLIENTE)));
        when(clienteGateway.findIdByUsuarioEmail("cliente@test.com"))
                .thenReturn(Optional.empty());

        ClienteAutenticadoNaoEncontradoException exception = assertThrows(
                ClienteAutenticadoNaoEncontradoException.class,
                () -> service.getClienteId());

        assertEquals("Cliente não encontrado para o usuário autenticado", exception.getMessage());
    }

    @Test
    void deveRetornar401QuandoNaoHouverUsuarioAtual() {
        when(currentUserGateway.getCurrentUser()).thenReturn(Optional.empty());

        UsuarioNaoAutenticadoException exception = assertThrows(
                UsuarioNaoAutenticadoException.class,
                () -> service.getClienteId());

        assertEquals("Usuário não autenticado", exception.getMessage());
    }
}
