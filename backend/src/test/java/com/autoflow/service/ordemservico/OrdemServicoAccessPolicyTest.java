package com.autoflow.service.ordemservico;

import com.autoflow.domain.ordemservico.DiagnosticoEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.service.ordemservico.impl.OrdemServicoAccessPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdemServicoAccessPolicyTest {

    private final OrdemServicoAccessPolicy policy = new OrdemServicoAccessPolicy();

    @Test
    void devePermitirAdminAlterarDiagnostico() {
        OrdemServicoEntity os = new OrdemServicoEntity();
        UsuarioEntity admin = usuario(1L, RoleEnum.ADMIN);

        assertDoesNotThrow(() -> policy.validarPodeAlterarDiagnostico(os, admin));
    }

    @Test
    void deveRetornarBadRequestQuandoNaoHaMecanicoAtribuido() {
        OrdemServicoEntity os = new OrdemServicoEntity();
        UsuarioEntity mecanico = usuario(2L, RoleEnum.MECANICO);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> policy.validarPodeAlterarDiagnostico(os, mecanico)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void deveRetornarForbiddenQuandoMecanicoLogadoNaoEhOAtribuido() {
        OrdemServicoEntity os = new OrdemServicoEntity();
        DiagnosticoEntity diagnostico = new DiagnosticoEntity();
        diagnostico.setMecanico(usuario(10L, RoleEnum.MECANICO));
        os.setDiagnostico(diagnostico);

        UsuarioEntity mecanicoLogado = usuario(11L, RoleEnum.MECANICO);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> policy.validarPodeAlterarDiagnostico(os, mecanicoLogado)
        );

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    private static UsuarioEntity usuario(Long id, RoleEnum role) {
        UsuarioEntity u = new UsuarioEntity();
        u.setId(id);
        u.setRole(role);
        u.setEmail("u" + id + "@exemplo.com");
        u.setNome("U" + id);
        return u;
    }
}

