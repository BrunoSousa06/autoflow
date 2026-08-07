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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecidirOrcamentoUseCaseTest {

    @Mock private OrcamentoGateway orcamentoGateway;
    @Mock private UsuarioGateway usuarioGateway;
    @Mock private AprovarOrcamentoUseCase aprovarOrcamentoUseCase;
    @Mock private RecusarOrcamentoUseCase recusarOrcamentoUseCase;
    @InjectMocks private DecidirOrcamentoUseCase useCase;

    @Test
    void deveAprovarAposAutorizarUsuario() {
        OrcamentoEntity orcamento = orcamento();
        when(orcamentoGateway.findById(10L)).thenReturn(Optional.of(orcamento));
        when(usuarioGateway.findByEmail("cliente@exemplo.com")).thenReturn(Optional.of(usuario("Maria", RoleEnum.CLIENTE)));
        when(aprovarOrcamentoUseCase.execute(orcamento, "Maria")).thenReturn(orcamento);

        assertSame(orcamento, useCase.aprovarComoUsuario(10L, "cliente@exemplo.com"));

        verify(aprovarOrcamentoUseCase).execute(orcamento, "Maria");
    }

    @Test
    void deveRecusarAposAutorizarUsuario() {
        OrcamentoEntity orcamento = orcamento();
        when(orcamentoGateway.findById(10L)).thenReturn(Optional.of(orcamento));
        when(usuarioGateway.findByEmail("cliente@exemplo.com")).thenReturn(Optional.of(usuario("Maria", RoleEnum.CLIENTE)));
        when(recusarOrcamentoUseCase.execute(orcamento, "Não quero", "Maria")).thenReturn(orcamento);

        assertSame(orcamento, useCase.recusarComoUsuario(10L, "Não quero", "cliente@exemplo.com"));

        verify(recusarOrcamentoUseCase).execute(orcamento, "Não quero", "Maria");
    }

    @Test
    void deveNegarDecisaoDeClienteSobreOrcamentoDeOutroCliente() {
        OrcamentoEntity orcamento = orcamento();
        when(orcamentoGateway.findById(10L)).thenReturn(Optional.of(orcamento));
        when(usuarioGateway.findByEmail("outro@exemplo.com")).thenReturn(Optional.of(usuario("Outro", RoleEnum.CLIENTE)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> useCase.aprovarComoUsuario(10L, "outro@exemplo.com"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void deveNegarDecisaoDeMecanicoMesmoQuandoOrcamentoExiste() {
        OrcamentoEntity orcamento = orcamento();
        when(orcamentoGateway.findById(10L)).thenReturn(Optional.of(orcamento));
        when(usuarioGateway.findByEmail("mecanico@exemplo.com"))
                .thenReturn(Optional.of(usuario("Mecânico", RoleEnum.MECANICO)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> useCase.aprovarComoUsuario(10L, "mecanico@exemplo.com"));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void deveAprovarPorAcompanhamentoComNomeDoCliente() {
        OrcamentoEntity orcamento = orcamento();
        when(orcamentoGateway.findById(10L)).thenReturn(Optional.of(orcamento));
        when(aprovarOrcamentoUseCase.execute(orcamento, "Cliente")).thenReturn(orcamento);

        assertSame(orcamento, useCase.aprovarDaOrdem(10L, "OS-123"));

        verify(aprovarOrcamentoUseCase).execute(orcamento, "Cliente");
    }

    private OrcamentoEntity orcamento() {
        OrcamentoEntity orcamento = new OrcamentoEntity();
        orcamento.setId(10L);
        orcamento.setNumeroOs("OS-123");
        orcamento.setCliente(ClienteOrcamentoSnapshot.builder().nome("Cliente").email("cliente@exemplo.com").build());
        return orcamento;
    }

    private UsuarioEntity usuario(String nome, RoleEnum role) {
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setNome(nome);
        usuario.setRole(role);
        return usuario;
    }
}
