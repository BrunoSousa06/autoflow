package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.ordemservico.Diagnostico;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtribuirMecanicoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;
    @Mock
    private UsuarioGateway usuarioGateway;

    private static Usuario usuario(Long id, RoleEnum role) {
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setRole(role);
        usuario.setEmail("usuario" + id + "@autoflow.com");
        return usuario;
    }

    private static void assertType(ApplicationException.ErrorType type, Executable executable) {
        assertEquals(type, assertThrows(ApplicationException.class, executable).type());
    }

    @Test
    void deveAtribuirMecanicoPorIdCriandoDiagnostico() {
        OrdemServico ordem = new OrdemServico();
        Usuario mecanico = usuario(10L, RoleEnum.MECANICO);
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(ordem));
        when(usuarioGateway.findById(10L)).thenReturn(Optional.of(mecanico));
        when(ordemServicoGateway.save(ordem)).thenReturn(ordem);

        assertSame(ordem, new AtribuirMecanicoUseCaseImpl(ordemServicoGateway, usuarioGateway)
                .execute("OS-1", 10L, null));

        assertSame(mecanico, ordem.getDiagnostico().getMecanico());
    }

    @Test
    void deveAtribuirMecanicoPorEmailMantendoDiagnosticoExistente() {
        OrdemServico ordem = new OrdemServico();
        Diagnostico diagnostico = new Diagnostico();
        ordem.setDiagnostico(diagnostico);
        Usuario mecanico = usuario(11L, RoleEnum.MECANICO);
        when(ordemServicoGateway.findByNumeroOs("OS-2")).thenReturn(Optional.of(ordem));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com"))
                .thenReturn(Optional.of(mecanico));
        when(ordemServicoGateway.save(ordem)).thenReturn(ordem);

        new AtribuirMecanicoUseCaseImpl(ordemServicoGateway, usuarioGateway)
                .execute("OS-2", null, "mecanico@autoflow.com");

        assertSame(diagnostico, ordem.getDiagnostico());
        assertSame(mecanico, diagnostico.getMecanico());
    }

    @Test
    void deveRejeitarOsMecanicoInvalidoEParametrosAusentes() {
        when(ordemServicoGateway.findByNumeroOs("ausente")).thenReturn(Optional.empty());
        assertType(ApplicationException.ErrorType.NOT_FOUND, () -> new AtribuirMecanicoUseCaseImpl(
                ordemServicoGateway, usuarioGateway).execute("ausente", 1L, null));

        OrdemServico ordem = new OrdemServico();
        when(ordemServicoGateway.findByNumeroOs("OS-3")).thenReturn(Optional.of(ordem));
        when(usuarioGateway.findById(12L)).thenReturn(Optional.empty());
        assertType(ApplicationException.ErrorType.NOT_FOUND, () -> new AtribuirMecanicoUseCaseImpl(
                ordemServicoGateway, usuarioGateway).execute("OS-3", 12L, null));

        when(usuarioGateway.findByEmail("ausente@autoflow.com"))
                .thenReturn(Optional.empty());
        assertType(ApplicationException.ErrorType.NOT_FOUND, () -> new AtribuirMecanicoUseCaseImpl(
                ordemServicoGateway, usuarioGateway).execute("OS-3", null, "ausente@autoflow.com"));

        assertType(ApplicationException.ErrorType.BAD_REQUEST, () -> new AtribuirMecanicoUseCaseImpl(
                ordemServicoGateway, usuarioGateway).execute("OS-3", null, "  "));

        Usuario atendente = usuario(13L, RoleEnum.ATENDENTE);
        when(usuarioGateway.findById(13L)).thenReturn(Optional.of(atendente));
        assertType(ApplicationException.ErrorType.BAD_REQUEST, () -> new AtribuirMecanicoUseCaseImpl(
                ordemServicoGateway, usuarioGateway).execute("OS-3", 13L, null));

        assertType(ApplicationException.ErrorType.BAD_REQUEST, () -> new AtribuirMecanicoUseCaseImpl(
                ordemServicoGateway, usuarioGateway).execute("OS-3", null, null));
    }

    @FunctionalInterface
    private interface Executable extends org.junit.jupiter.api.function.Executable {
    }
}
