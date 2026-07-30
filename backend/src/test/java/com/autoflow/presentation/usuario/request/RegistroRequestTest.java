package com.autoflow.presentation.usuario.request;

import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.presentation.usuario.request.RegistroRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistroRequestTest {

    @Test
    void deveCriarRequestComTodosOsCampos() {
        RegistroRequest request = new RegistroRequest(
                "João Silva", "joao@email.com", "12345678901",
                "11999999999", "Senha@123", RoleEnum.ADMIN
        );

        assertEquals("João Silva", request.nome());
        assertEquals("joao@email.com", request.email());
        assertEquals("12345678901", request.cpfCnpj());
        assertEquals("11999999999", request.telefone());
        assertEquals("Senha@123", request.senha());
        assertEquals(RoleEnum.ADMIN, request.role());
    }

    @Test
    void deveDefaultarRoleParaClienteQuandoNulo() {
        RegistroRequest request = new RegistroRequest(
                "João Silva", "joao@email.com", "12345678901",
                "11999999999", "Senha@123", null
        );

        assertEquals(RoleEnum.CLIENTE, request.role());
    }

    @Test
    void deveMantherRoleMecanicoQuandoInformado() {
        RegistroRequest request = new RegistroRequest(
                "João", "joao@email.com", "12345678901",
                "11999999999", "Senha@123", RoleEnum.MECANICO
        );

        assertEquals(RoleEnum.MECANICO, request.role());
    }

    @Test
    void deveMantherRoleAtendenteQuandoInformado() {
        RegistroRequest request = new RegistroRequest(
                "João", "joao@email.com", "12345678901",
                "11999999999", "Senha@123", RoleEnum.ATENDENTE
        );

        assertEquals(RoleEnum.ATENDENTE, request.role());
    }

    @Test
    void deveSerIgualQuandoMesmosValores() {
        RegistroRequest r1 = new RegistroRequest("João", "joao@email.com", "12345678901", "11999999999", "Senha@123", RoleEnum.CLIENTE);
        RegistroRequest r2 = new RegistroRequest("João", "joao@email.com", "12345678901", "11999999999", "Senha@123", RoleEnum.CLIENTE);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
