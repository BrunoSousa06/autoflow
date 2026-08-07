package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.gateway.UsuarioGateway;
import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class AtribuirMecanicoUseCaseTest {

    @Mock
    private OrdemServicoGateway ordemServicoGateway;
    @Mock
    private UsuarioGateway usuarioGateway;

    @Test
    void deveAtribuirMecanicoPorIdCriandoDiagnostico() {
        OrdemServicoEntity ordem = new OrdemServicoEntity();
        UsuarioEntity mecanico = usuario(10L, RoleEnum.MECANICO);
        when(ordemServicoGateway.findByNumeroOs("OS-1")).thenReturn(Optional.of(ordem));
        when(usuarioGateway.findById(10L)).thenReturn(Optional.of(mecanico));
        when(ordemServicoGateway.save(ordem)).thenReturn(ordem);

        assertSame(ordem, new AtribuirMecanicoUseCase(ordemServicoGateway, usuarioGateway)
                .execute("OS-1", 10L, null));

        assertSame(mecanico, ordem.getDiagnostico().getMecanico());
    }

    @Test
    void deveAtribuirMecanicoPorEmailMantendoDiagnosticoExistente() {
        OrdemServicoEntity ordem = new OrdemServicoEntity();
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        ordem.setDiagnostico(diagnostico);
        UsuarioEntity mecanico = usuario(11L, RoleEnum.MECANICO);
        when(ordemServicoGateway.findByNumeroOs("OS-2")).thenReturn(Optional.of(ordem));
        when(usuarioGateway.findByEmail("mecanico@autoflow.com"))
                .thenReturn(Optional.of(mecanico));
        when(ordemServicoGateway.save(ordem)).thenReturn(ordem);

        new AtribuirMecanicoUseCase(ordemServicoGateway, usuarioGateway)
                .execute("OS-2", null, "mecanico@autoflow.com");

        assertSame(diagnostico, ordem.getDiagnostico());
        assertSame(mecanico, diagnostico.getMecanico());
    }

    @Test
    void deveRejeitarOsMecanicoInvalidoEParametrosAusentes() {
        when(ordemServicoGateway.findByNumeroOs("ausente")).thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> new AtribuirMecanicoUseCase(
                ordemServicoGateway, usuarioGateway).execute("ausente", 1L, null));

        OrdemServicoEntity ordem = new OrdemServicoEntity();
        when(ordemServicoGateway.findByNumeroOs("OS-3")).thenReturn(Optional.of(ordem));
        when(usuarioGateway.findById(12L)).thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> new AtribuirMecanicoUseCase(
                ordemServicoGateway, usuarioGateway).execute("OS-3", 12L, null));

        when(usuarioGateway.findByEmail("ausente@autoflow.com"))
                .thenReturn(Optional.empty());
        assertStatus(HttpStatus.NOT_FOUND, () -> new AtribuirMecanicoUseCase(
                ordemServicoGateway, usuarioGateway).execute("OS-3", null, "ausente@autoflow.com"));

        assertStatus(HttpStatus.BAD_REQUEST, () -> new AtribuirMecanicoUseCase(
                ordemServicoGateway, usuarioGateway).execute("OS-3", null, "  "));

        UsuarioEntity atendente = usuario(13L, RoleEnum.ATENDENTE);
        when(usuarioGateway.findById(13L)).thenReturn(Optional.of(atendente));
        assertStatus(HttpStatus.BAD_REQUEST, () -> new AtribuirMecanicoUseCase(
                ordemServicoGateway, usuarioGateway).execute("OS-3", 13L, null));

        assertStatus(HttpStatus.BAD_REQUEST, () -> new AtribuirMecanicoUseCase(
                ordemServicoGateway, usuarioGateway).execute("OS-3", null, null));
    }

    private static UsuarioEntity usuario(Long id, RoleEnum role) {
        var usuario = new UsuarioEntity();
        usuario.setId(id);
        usuario.setRole(role);
        usuario.setEmail("usuario" + id + "@autoflow.com");
        return usuario;
    }

    private static void assertStatus(HttpStatus status, Executable executable) {
        assertEquals(status, assertThrows(ResponseStatusException.class, executable).getStatusCode());
    }

    @FunctionalInterface
    private interface Executable extends org.junit.jupiter.api.function.Executable {
    }
}
