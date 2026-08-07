package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.dto.orcamento.OrcamentoFiltro;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarOrcamentosUseCaseTest {
    @Mock OrcamentoGateway orcamentoGateway;
    @Mock UsuarioGateway usuarioGateway;
    @InjectMocks ConsultarOrcamentosUseCase useCase;

    @Test
    void deveForcarEmailDoClienteNoFiltro() {
        UsuarioEntity usuario = usuario("cliente@exemplo.com", RoleEnum.CLIENTE);
        OrcamentoFiltro esperado = new OrcamentoFiltro(null, null, null, "cliente@exemplo.com", null, null);
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(orcamentoGateway.findAll(esperado)).thenReturn(List.of());

        assertTrue(useCase.execute(usuario.getEmail(), null).isEmpty());
        verify(orcamentoGateway).findAll(esperado);
    }

    @Test
    void deveNegarFiltroDeOutroCliente() {
        UsuarioEntity usuario = usuario("cliente@exemplo.com", RoleEnum.CLIENTE);
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        OrcamentoFiltro filtro = new OrcamentoFiltro(null, null, null, "outro@exemplo.com", null, null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> useCase.execute(usuario.getEmail(), filtro));
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verifyNoInteractions(orcamentoGateway);
    }

    private UsuarioEntity usuario(String email, RoleEnum role) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setEmail(email); usuario.setRole(role); return usuario;
    }
}
