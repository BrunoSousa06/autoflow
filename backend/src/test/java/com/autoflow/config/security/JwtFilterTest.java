package com.autoflow.config.security;

import com.autoflow.infrastructure.security.JwtFilter;
import com.autoflow.infrastructure.security.service.CustomUserDetailsService;
import com.autoflow.infrastructure.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private UserDetails userDetails;

    @BeforeEach
    void setup() {

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();

        userDetails = new User(
                "teste@email.com",
                "123456",
                List.of()
        );
    }

    @Test
    void deveContinuarFiltroQuandoAuthorizationForNulo() {

        assertDoesNotThrow(this::executarFiltro);

        assertDoesNotThrow(this::verificarFiltroContinuou);

        verifyNoInteractions(
                jwtService,
                userDetailsService
        );
    }

    @Test
    void deveContinuarFiltroQuandoHeaderNaoForBearer() {

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        assertDoesNotThrow(this::executarFiltro);

        assertDoesNotThrow(this::verificarFiltroContinuou);

        verifyNoInteractions(
                jwtService,
                userDetailsService
        );
    }

    @Test
    void deveAutenticarUsuarioQuandoTokenForValido() {

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.tokenValido(token))
                .thenReturn(true);

        when(jwtService.extrairEmail(token))
                .thenReturn("teste@email.com");

        when(userDetailsService.loadUserByUsername("teste@email.com"))
                .thenReturn(userDetails);

        assertDoesNotThrow(this::executarFiltro);

        assertNotNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        assertEquals(
                "teste@email.com",
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName()
        );

        verify(jwtService)
                .extrairEmail(token);

        verify(jwtService)
                .tokenValido(token);

        verify(userDetailsService)
                .loadUserByUsername("teste@email.com");

        assertDoesNotThrow(this::verificarFiltroContinuou);
    }

    @Test
    void naoDeveAutenticarQuandoEmailForNulo() {

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.tokenValido(token)).thenReturn(true);
        when(jwtService.extrairEmail(token)).thenReturn(null);

        assertDoesNotThrow(this::executarFiltro);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extrairEmail(token);

        verifyNoInteractions(userDetailsService);

        assertDoesNotThrow(this::verificarFiltroContinuou);
    }

    @Test
    void naoDeveAutenticarQuandoTokenForInvalido() {

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.tokenValido(token))
                .thenReturn(false);

        assertDoesNotThrow(this::executarFiltro);

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService, never()).extrairEmail(token);
        verifyNoInteractions(userDetailsService);

        assertDoesNotThrow(this::verificarFiltroContinuou);
    }

    @Test
    void naoDeveCarregarUsuarioQuandoJaExistirAutenticacao() {

        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                "usuario",
                                null,
                                List.of()
                        )
                );

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        assertDoesNotThrow(this::executarFiltro);

        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);

        assertDoesNotThrow(this::verificarFiltroContinuou);
    }

    @Test
    void deveDefinirRoleNoTokenDeAutenticacao() {

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token)).thenReturn("teste@email.com");
        when(userDetailsService.loadUserByUsername("teste@email.com")).thenReturn(userDetails);
        when(jwtService.tokenValido(token)).thenReturn(true);
        when(jwtService.extrairRole(token)).thenReturn("ADMIN");

        assertDoesNotThrow(this::executarFiltro);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @ParameterizedTest(name = "Deve ignorar o filtro JWT para a rota: {0}")
    @ValueSource(strings = {
            "/public/ordens-servico/acompanhamento",
            "/public/outra-rota",
            "/auth/login",
            "/actuator/health",
            "/actuator/health/liveness",
            "/actuator/health/readiness",
            "/swagger-ui/index.html",
            "/v3/api-docs/swagger-config",
            "/swagger-ui.html"
    })
    void shouldNotFilterDeveRetornarTrueParaRotasPublicas(String rota) {
        request.setRequestURI(rota);

        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterDeveRetornarFalseParaOutrasRotas()
            throws ServletException {

        request.setRequestURI("/api/clientes");
        assertFalse(jwtFilter.shouldNotFilter(request));
    }

    private void executarFiltro() throws ServletException, IOException {
        jwtFilter.doFilterInternal(request, response, filterChain);
    }

    private void verificarFiltroContinuou() throws ServletException, IOException {
        verify(filterChain).doFilter(request, response);
    }
}
