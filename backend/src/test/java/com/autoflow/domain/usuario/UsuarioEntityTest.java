package com.autoflow.domain.usuario;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioEntityTest {

    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setSenha("hashed_password_123");
        usuario.setRole(RoleEnum.CLIENTE);
    }

    @Test
    void testUsuarioCreation() {
        assertNotNull(usuario);
        assertEquals(1L, usuario.getId());
        assertEquals("João Silva", usuario.getNome());
        assertEquals("joao@example.com", usuario.getEmail());
        assertEquals("hashed_password_123", usuario.getSenha());
        assertEquals(RoleEnum.CLIENTE, usuario.getRole());
    }

    @Test
    void testUsuarioSetters() {
        usuario.setNome("Maria Silva");
        usuario.setEmail("maria@example.com");
        usuario.setSenha("new_password_456");
        usuario.setRole(RoleEnum.ADMIN);

        assertEquals("Maria Silva", usuario.getNome());
        assertEquals("maria@example.com", usuario.getEmail());
        assertEquals("new_password_456", usuario.getSenha());
        assertEquals(RoleEnum.ADMIN, usuario.getRole());
    }

    @Test
    void testUsuarioEmailUniqueness() {
        UsuarioEntity usuario2 = new UsuarioEntity();
        usuario2.setEmail("joao@example.com");

        assertEquals(usuario.getEmail(), usuario2.getEmail());
    }

    @Test
    void testUsuarioRoleEnum() {
        for (RoleEnum role : RoleEnum.values()) {
            usuario.setRole(role);
            assertEquals(role, usuario.getRole());
        }
    }

    @Test
    void testUsuarioNullableFields() {
        UsuarioEntity novoUsuario = new UsuarioEntity();
        assertNull(novoUsuario.getNome());
        assertNull(novoUsuario.getEmail());
        assertNull(novoUsuario.getSenha());
        assertNull(novoUsuario.getRole());
    }

    @Test
    void testUsuarioWithAllRoles() {
        usuario.setRole(RoleEnum.ADMIN);
        assertEquals(RoleEnum.ADMIN, usuario.getRole());

        usuario.setRole(RoleEnum.ATENDENTE);
        assertEquals(RoleEnum.ATENDENTE, usuario.getRole());

        usuario.setRole(RoleEnum.MECANICO);
        assertEquals(RoleEnum.MECANICO, usuario.getRole());

        usuario.setRole(RoleEnum.CLIENTE);
        assertEquals(RoleEnum.CLIENTE, usuario.getRole());
    }
}
