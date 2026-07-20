package com.autoflow.application.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioAutenticadoServiceTest {

    private final UsuarioAutenticadoService service = new UsuarioAutenticadoService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRetornarAuthenticationDoSecurityContext() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "teste@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertSame(authentication, service.getAuthentication());
    }

    @Test
    void deveRetornarEmailDoUsuarioAutenticado() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "teste@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals("teste@email.com", service.getEmail());
    }

    @Test
    void deveRetornarTrueQuandoUsuarioForCliente() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "cliente@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(service.isCliente());
    }

    @Test
    void deveRetornarFalseQuandoUsuarioNaoForCliente() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertFalse(service.isCliente());
    }

    @Test
    void deveRetornarTrueQuandoUsuarioForAdministrador() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(service.isAdministrador());
    }

    @Test
    void deveRetornarFalseQuandoUsuarioNaoForAdministrador() {
        var authentication = new UsernamePasswordAuthenticationToken(
                "cliente@email.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENTE"))
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertFalse(service.isAdministrador());
    }
}
