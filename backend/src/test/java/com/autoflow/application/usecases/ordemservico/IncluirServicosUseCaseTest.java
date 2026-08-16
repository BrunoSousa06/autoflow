package com.autoflow.application.usecases.ordemservico;

import com.autoflow.domain.servico.Servico;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.ServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.application.policy.OrdemServicoAccessPolicy;
import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncluirServicosUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;
    @Mock
    private ServicoGateway servicoGateway;
    @Mock
    private UsuarioGateway usuarioGateway;
    @Mock
    private OrdemServicoAccessPolicy accessPolicy;

    @Test
    void deveIncluirServicoParaAdminDuranteDiagnostico() {
        OrdemServicoEntity ordem = ordem(StatusOrdemServico.EM_DIAGNOSTICO);
        Servico catalogo = servico(5L);
        UsuarioEntity admin = usuario(RoleEnum.ADMIN, 1L);
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(ordem));
        when(usuarioGateway.findByEmail("admin@autoflow.com"))
                .thenReturn(Optional.of(admin));
        when(servicoGateway.findById(5L)).thenReturn(Optional.of(catalogo));
        when(ordemServicoGateway.save(ordem)).thenReturn(ordem);

        var resultado = new IncluirServicosUseCase(ordemServicoGateway, servicoGateway,
                usuarioGateway, accessPolicy).execute("OS-1",
                List.of(new ServicoSolicitadoEntity(5L)), "admin@autoflow.com");

        assertEquals(1, resultado.getServicosSolicitados().size());
        assertEquals("Troca de oleo", resultado.getServicosSolicitados().get(0).getNome());
        verify(accessPolicy, never()).validarPodeAlterarDiagnostico(ordem, admin);
    }

    @Test
    void deveValidarMecanicoEIncluirServicoForaDoDiagnostico() {
        OrdemServicoEntity ordem = ordem(StatusOrdemServico.EM_DIAGNOSTICO);
        ordem.setDiagnostico(new DiagnosticoEntity());
        UsuarioEntity mecanico = usuario(RoleEnum.MECANICO, 2L);
        Servico catalogo = servico(6L);
        when(ordemServicoGateway.findByNumeroOs("OS-2")).thenReturn(Optional.of(ordem));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com"))
                .thenReturn(Optional.of(mecanico));
        when(servicoGateway.findById(6L)).thenReturn(Optional.of(catalogo));
        when(servicoGateway.findById(7L)).thenReturn(Optional.of(servico(7L)));
        when(ordemServicoGateway.save(ordem)).thenReturn(ordem);

        new IncluirServicosUseCase(ordemServicoGateway, servicoGateway,
                usuarioGateway, accessPolicy).execute("OS-2",
                List.of(new ServicoSolicitadoEntity(6L)), "mecanico@autoflow.com");

        verify(accessPolicy).validarPodeAlterarDiagnostico(ordem, mecanico);

        ordem.setStatus(StatusOrdemServico.RECEBIDA);
        new IncluirServicosUseCase(ordemServicoGateway, servicoGateway,
                usuarioGateway, accessPolicy).execute("OS-2",
                List.of(new ServicoSolicitadoEntity(7L)), "nao-consulta@autoflow.com");
        verify(usuarioGateway, never()).findByEmail("nao-consulta@autoflow.com");
    }

    private static void assertType(ApplicationException.ErrorType type,
                                   org.junit.jupiter.api.function.Executable executable) {
        assertEquals(type, assertThrows(ApplicationException.class, executable).type());
    }

    private OrdemServicoEntity incluir(String numeroOs, List<ServicoSolicitadoEntity> servicos,
                                       String email) {
        return new IncluirServicosUseCase(ordemServicoGateway, servicoGateway,
                usuarioGateway, accessPolicy).execute(numeroOs, servicos, email);
    }

    private static OrdemServicoEntity ordem(StatusOrdemServico status) {
        var ordem = new OrdemServicoEntity();
        ordem.setStatus(status);
        return ordem;
    }

    private static Servico servico(Long id) {
        return Servico.reconstituir(id, "Troca de oleo", "Descricao", new BigDecimal("100.00"), true);
    }

    private static UsuarioEntity usuario(RoleEnum role, Long id) {
        var usuario = new UsuarioEntity();
        usuario.setId(id);
        usuario.setRole(role);
        return usuario;
    }

    @Test
    void deveRejeitarEntradasEReferenciasInexistentes() {
        when(ordemServicoGateway.findByNumeroOs("ausente")).thenReturn(Optional.empty());
        assertType(ApplicationException.ErrorType.NOT_FOUND, () -> incluir("ausente", List.of(), "admin"));

        OrdemServicoEntity ordem = ordem(StatusOrdemServico.RECEBIDA);
        when(ordemServicoGateway.findByNumeroOs("OS-3")).thenReturn(Optional.of(ordem));
        assertThrows(IllegalArgumentException.class,
                () -> incluir("OS-3", null, "admin"));
        assertThrows(IllegalArgumentException.class,
                () -> incluir("OS-3", List.of(), "admin"));

        ordem.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        when(usuarioGateway.findByEmail("ausente"))
                .thenReturn(Optional.empty());
        assertType(ApplicationException.ErrorType.NOT_FOUND,
                () -> incluir("OS-3", List.of(new ServicoSolicitadoEntity(5L)), "ausente"));

        when(usuarioGateway.findByEmail("atendente"))
                .thenReturn(Optional.of(usuario(RoleEnum.ATENDENTE, 3L)));
        when(servicoGateway.findById(5L)).thenReturn(Optional.empty());
        assertType(ApplicationException.ErrorType.NOT_FOUND,
                () -> incluir("OS-3", List.of(new ServicoSolicitadoEntity(5L)), "atendente"));
    }
}
