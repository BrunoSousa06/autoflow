package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.orcamento.ClienteOrcamentoSnapshot;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarOrcamentoAutenticadoUseCaseTest {

    @Mock
    private OrcamentoGateway orcamentoGateway;

    @Mock
    private UsuarioGateway usuarioGateway;

    @InjectMocks
    private ConsultarOrcamentoAutenticadoUseCase useCase;

    @Test
    void devePermitirClienteConsultarSeuProprioOrcamento() {
        var orcamento = orcamentoDoCliente("cliente@autoflow.com");
        var usuario = usuario(RoleEnum.CLIENTE, "cliente@autoflow.com");
        when(orcamentoGateway.findById(1L)).thenReturn(Optional.of(orcamento));
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertSame(orcamento, useCase.execute(1L, usuario.getEmail()));
    }

    @Test
    void deveNegarClienteConsultarOrcamentoDeOutroCliente() {
        var orcamento = orcamentoDoCliente("outro@autoflow.com");
        var usuario = usuario(RoleEnum.CLIENTE, "cliente@autoflow.com");
        when(orcamentoGateway.findById(1L)).thenReturn(Optional.of(orcamento));
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        var exception = assertThrows(ResponseStatusException.class,
                () -> useCase.execute(1L, usuario.getEmail()));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void devePermitirEquipeConsultarOrcamentoDeQualquerCliente() {
        var orcamento = orcamentoDoCliente("cliente@autoflow.com");
        var usuario = usuario(RoleEnum.ATENDENTE, "atendente@autoflow.com");
        when(orcamentoGateway.findById(1L)).thenReturn(Optional.of(orcamento));
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));

        assertSame(orcamento, useCase.execute(1L, usuario.getEmail()));
    }

    @Test
    void deveRetornar404QuandoOrcamentoNaoExiste() {
        when(orcamentoGateway.findById(1L)).thenReturn(Optional.empty());

        var exception = assertThrows(ResponseStatusException.class,
                () -> useCase.execute(1L, "cliente@autoflow.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private OrcamentoEntity orcamentoDoCliente(String email) {
        var orcamento = new OrcamentoEntity();
        orcamento.setCliente(ClienteOrcamentoSnapshot.builder().email(email).build());
        return orcamento;
    }

    private UsuarioEntity usuario(RoleEnum role, String email) {
        var usuario = new UsuarioEntity();
        usuario.setRole(role);
        usuario.setEmail(email);
        return usuario;
    }
}
