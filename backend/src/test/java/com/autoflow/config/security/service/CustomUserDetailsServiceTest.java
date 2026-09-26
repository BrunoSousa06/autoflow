package com.autoflow.config.security.service;

import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.application.policy.ClienteAtivoPolicy;
import com.autoflow.infrastructure.persistence.entity.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.repository.UsuarioRepository;
import com.autoflow.infrastructure.security.service.CustomUserDetailsService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteAtivoPolicy clienteAtivoPolicy;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private UsuarioEntity usuario;

    @BeforeEach
    void setup() {

        usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setEmail("admin@email.com");
        usuario.setCpfCnpj("12345678980");
        usuario.setSenha("senhaCriptografada");
        usuario.setRole(RoleEnum.ADMIN);
    }

    @Test
    void deveCarregarUsuarioPorEmail() {

        when(usuarioRepository.findByCpfCnpj("12345678980"))
                .thenReturn(Optional.of(usuario));

        UserDetails resultado =
                customUserDetailsService.loadUserByUsername(
                        "12345678980"
                );

        assertNotNull(resultado);

        assertEquals(
                "12345678980",
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
                                        .equals("ROLE_ADMIN"))
        );

        verify(usuarioRepository)
                .findByCpfCnpj("12345678980");
    }

    @Test
    void deveCarregarClienteAtivoHabilitado() {
        usuario.setEmail("cliente@email.com");
        usuario.setRole(RoleEnum.CLIENTE);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(clienteAtivoPolicy.podeAutenticar(usuario.getEmail(), true)).thenReturn(true);

        UserDetails resultado = customUserDetailsService.loadUserByUsername(usuario.getEmail());

        assertTrue(resultado.isEnabled());
    }

    @Test
    void deveCarregarClienteInativoDesabilitado() {
        usuario.setEmail("cliente@email.com");
        usuario.setRole(RoleEnum.CLIENTE);
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(Optional.of(usuario));
        when(clienteAtivoPolicy.podeAutenticar(usuario.getEmail(), true)).thenReturn(false);

        UserDetails resultado = customUserDetailsService.loadUserByUsername(usuario.getEmail());

        assertFalse(resultado.isEnabled());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {

        when(usuarioRepository.findByCpfCnpj("12345678980"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception =
                assertThrows(
                        UsernameNotFoundException.class,
                        () -> customUserDetailsService
                                .loadUserByUsername(
                                        "12345678980"
                                )
                );

        assertEquals(
                "Usuário não encontrado",
                exception.getMessage()
        );

        verify(usuarioRepository)
                .findByCpfCnpj("12345678980");
    }
}
