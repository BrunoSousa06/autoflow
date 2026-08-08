package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.HistoricoStatusOsGateway;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoNotificacaoGateway;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.application.gateway.OrcamentoVersioningGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.application.usecases.orcamento.OrcamentoFactory;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinalizarDiagnosticoUseCaseTest {

    @Mock private OrdemServicoGateway ordemServicoGateway;
    @Mock private UsuarioGateway usuarioGateway;
    @Mock private OrdemServicoAccessPolicy accessPolicy;
    @Mock private OrcamentoVersioningGateway versioningGateway;
    @Mock private OrcamentoFactory orcamentoFactory;
    @Mock private OrcamentoGateway orcamentoGateway;
    @Mock private OrcamentoPublicacaoGateway publicacaoGateway;
    @Mock private OrcamentoNotificacaoGateway notificacaoGateway;
    @Mock private HistoricoStatusOsGateway historicoGateway;

    @Test
    void deveFinalizarDiagnosticoSemValidarPermissaoParaAdmin() {
        var os = ordemEmDiagnostico();
        var admin = usuario(RoleEnum.ADMIN);
        configurarFluxo(os, admin);

        var resultado = novoCasoDeUso().execute("OS-1", admin.getEmail());

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, resultado.ordemServico().getStatus());
        verify(accessPolicy, never()).validarPodeAlterarDiagnostico(any(), any());
    }

    @Test
    void deveValidarPermissaoAoFinalizarDiagnosticoComoMecanico() {
        var os = ordemEmDiagnostico();
        var mecanico = usuario(RoleEnum.MECANICO);
        configurarFluxo(os, mecanico);

        novoCasoDeUso().execute("OS-1", mecanico.getEmail());

        verify(accessPolicy).validarPodeAlterarDiagnostico(os, mecanico);
    }

    private FinalizarDiagnosticoUseCase novoCasoDeUso() {
        return new FinalizarDiagnosticoUseCase(
                ordemServicoGateway, usuarioGateway, accessPolicy, versioningGateway,
                orcamentoFactory, orcamentoGateway, publicacaoGateway,
                notificacaoGateway, historicoGateway
        );
    }

    private void configurarFluxo(OrdemServicoEntity os, UsuarioEntity usuario) {
        var orcamento = new OrcamentoEntity();
        orcamento.setId(10L);
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(os));
        when(usuarioGateway.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(versioningGateway.proximaVersaoPrincipalNumeroOs("OS-1")).thenReturn(1);
        when(orcamentoFactory.criarPrincipalDisponivel(eq(os), eq(1), any(LocalDateTime.class)))
                .thenReturn(orcamento);
        when(orcamentoGateway.save(orcamento)).thenReturn(orcamento);
        when(publicacaoGateway.publicar(10L)).thenReturn("https://autoflow.test/orcamento");
        when(ordemServicoGateway.save(os)).thenReturn(os);
    }

    private OrdemServicoEntity ordemEmDiagnostico() {
        var os = new OrdemServicoEntity();
        os.setId(1L);
        os.setNumeroOs("OS-1");
        os.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        var diagnostico = new DiagnosticoEntity();
        diagnostico.setLaudo("Laudo concluido");
        os.setDiagnostico(diagnostico);
        return os;
    }

    private UsuarioEntity usuario(RoleEnum role) {
        var usuario = new UsuarioEntity();
        usuario.setEmail(role == RoleEnum.ADMIN ? "admin@autoflow.com" : "mecanico@autoflow.com");
        usuario.setRole(role);
        return usuario;
    }
}
