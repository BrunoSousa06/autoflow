package com.autoflow.application.policy;

import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdemServicoAccessPolicyTest {

    private final OrdemServicoAccessPolicy policy = new OrdemServicoAccessPolicy();

    @Test
    void devePermitirAdminEOMecanicoAtribuido() {
        OrdemServicoEntity ordem = new OrdemServicoEntity();
        UsuarioEntity admin = usuario(1L, RoleEnum.ADMIN);
        assertDoesNotThrow(() -> policy.validarPodeAlterarDiagnostico(ordem, admin));

        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(usuario(2L, RoleEnum.MECANICO));
        ordem.setDiagnostico(diagnostico);
        assertDoesNotThrow(() -> policy.validarPodeAlterarDiagnostico(ordem,
                usuario(2L, RoleEnum.MECANICO)));
    }

    @Test
    void deveRejeitarUsuarioSemMecanicoAtribuido() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> policy.validarPodeAlterarDiagnostico(new OrdemServicoEntity(),
                        usuario(2L, RoleEnum.MECANICO)));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deveRejeitarMecanicoDiferenteDoAtribuido() {
        OrdemServicoEntity ordem = new OrdemServicoEntity();
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(usuario(2L, RoleEnum.MECANICO));
        ordem.setDiagnostico(diagnostico);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> policy.validarPodeAlterarDiagnostico(ordem,
                        usuario(3L, RoleEnum.MECANICO)));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    private static UsuarioEntity usuario(Long id, RoleEnum role) {
        var usuario = new UsuarioEntity();
        usuario.setId(id);
        usuario.setRole(role);
        return usuario;
    }
}
