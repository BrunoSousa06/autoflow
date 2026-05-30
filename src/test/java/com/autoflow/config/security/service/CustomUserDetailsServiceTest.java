package com.autoflow.config.security.service;

import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private UsuarioEntity usuario;

    @BeforeEach
    void setup() {

        usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("admin@email.com");
        usuario.setSenha("senhaCriptografada");
        usuario.setRole(RoleEnum.ADMIN);
    }

    @Test
    void deveCarregarUsuarioPorEmail() {

        when(usuarioRepository.findByEmail("admin@email.com"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado =
                customUserDetailsService.loadUserByUsername(
                        "admin@email.com"
                );

        assertNotNull(resultado);

        assertEquals(
                "admin@email.com",
                resultado.getUsername()
        );

        assertEquals(
                "senhaCriptografada",
                resultado.getPassword()
        );

        assertTrue(
                resultado.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ADMIN"))
        );

        verify(usuarioRepository)
                .findByEmail("admin@email.com");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {

        when(usuarioRepository.findByEmail("inexistente@email.com"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception =
                assertThrows(
                        UsernameNotFoundException.class,
                        () -> customUserDetailsService
                                .loadUserByUsername(
                                        "inexistente@email.com"
                                )
                );

        assertEquals(
                "Usuário não encontrado",
                exception.getMessage()
        );

        verify(usuarioRepository)
                .findByEmail("inexistente@email.com");
    }
}
