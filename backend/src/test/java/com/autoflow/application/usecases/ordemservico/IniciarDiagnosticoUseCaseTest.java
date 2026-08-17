package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IniciarDiagnosticoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private HistoricoStatusOsGateway historicoStatusOsGateway;

    @Mock
    private OrdemServicoAccessPolicy accessPolicy;

    @Test
    void deveIniciarDiagnosticoSemValidarPermissaoParaAdmin() {
        var os = ordemRecebida();
        var admin = usuario(RoleEnum.ADMIN);
        configurarBusca(os, admin);

        var resultado = new IniciarDiagnosticoUseCase(
                ordemServicoGateway, usuarioGateway, historicoStatusOsGateway, accessPolicy
        ).execute("OS-1", "admin@autoflow.com");

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, resultado.getStatus());
        verify(accessPolicy, never()).validarPodeAlterarDiagnostico(any(), any());
        verify(historicoStatusOsGateway).save(any());
    }

    @Test
    void deveValidarPermissaoParaMecanico() {
        var os = ordemRecebida();
        var mecanico = usuario(RoleEnum.MECANICO);
        configurarBusca(os, mecanico);

        new IniciarDiagnosticoUseCase(
                ordemServicoGateway, usuarioGateway, historicoStatusOsGateway, accessPolicy
        ).execute("OS-1", "mecanico@autoflow.com");

        verify(accessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    private void configurarBusca(OrdemServico os, Usuario usuario) {
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(ordemServicoGateway.save(os)).thenReturn(os);
    }

    private OrdemServico ordemRecebida() {
        var os = new OrdemServico();
        os.setId(1L);
        os.setNumeroOs("OS-1");
        os.setStatus(StatusOrdemServico.RECEBIDA);
        return os;
    }

    private Usuario usuario(RoleEnum role) {
        var usuario = new Usuario();
        usuario.setEmail(role == RoleEnum.ADMIN ? "admin@autoflow.com" : "mecanico@autoflow.com");
        usuario.setRole(role);
        return usuario;
    }
}
