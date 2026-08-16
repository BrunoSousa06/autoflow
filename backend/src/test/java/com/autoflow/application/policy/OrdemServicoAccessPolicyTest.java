package com.autoflow.application.policy;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoAccessPolicyTest {

    private final OrdemServicoAccessPolicy policy = new OrdemServicoAccessPolicy();

    private static Usuario usuario(Long id, RoleEnum role) {
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setRole(role);
        return usuario;
    }

    @Test
    void devePermitirAdminEOMecanicoAtribuido() {
        OrdemServicoEntity ordem = new OrdemServicoEntity();
        Usuario admin = usuario(1L, RoleEnum.ADMIN);
        assertDoesNotThrow(() -> policy.validarPodeAlterarDiagnostico(ordem, admin));

        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(usuario(2L, RoleEnum.MECANICO));
        ordem.setDiagnostico(diagnostico);
        assertDoesNotThrow(() -> policy.validarPodeAlterarDiagnostico(ordem,
                usuario(2L, RoleEnum.MECANICO)));
    }

    @Test
    void deveRejeitarUsuarioSemMecanicoAtribuido() {
        OrdemServicoEntity ordem = new OrdemServicoEntity();
        Usuario mecanico = usuario(2L, RoleEnum.MECANICO);
        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> policy.validarPodeAlterarDiagnostico(ordem, mecanico));

        assertEquals(ApplicationException.ErrorType.BAD_REQUEST, exception.type());
    }

    @Test
    void deveRejeitarMecanicoDiferenteDoAtribuido() {
        OrdemServicoEntity ordem = new OrdemServicoEntity();
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(usuario(2L, RoleEnum.MECANICO));
        ordem.setDiagnostico(diagnostico);
        Usuario mecanico = usuario(3L, RoleEnum.MECANICO);

        ApplicationException exception = assertThrows(ApplicationException.class,
                () -> policy.validarPodeAlterarDiagnostico(ordem, mecanico));

        assertEquals(ApplicationException.ErrorType.FORBIDDEN, exception.type());
    }
}
