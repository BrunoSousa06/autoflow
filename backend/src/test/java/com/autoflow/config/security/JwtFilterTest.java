package com.autoflow.config.security;

import com.autoflow.infrastructure.persistence.security.JwtFilter;
import com.autoflow.infrastructure.persistence.security.service.CustomUserDetailsService;
import com.autoflow.infrastructure.persistence.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    void deveContinuarFiltroQuandoAuthorizationForNulo()
            throws ServletException, IOException {

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(
                jwtService,
                userDetailsService
        );
    }

    @Test
    void deveContinuarFiltroQuandoHeaderNaoForBearer()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Basic abc123"
        );

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(filterChain)
                .doFilter(request, response);

        verifyNoInteractions(
                jwtService,
                userDetailsService
        );
    }

    @Test
    void deveAutenticarUsuarioQuandoTokenForValido()
            throws ServletException, IOException {

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token))
                .thenReturn("teste@email.com");

        when(userDetailsService.loadUserByUsername("teste@email.com"))
                .thenReturn(userDetails);

        when(jwtService.tokenValido(token))
                .thenReturn(true);

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

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

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarQuandoEmailForNulo()
            throws ServletException, IOException {

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token))
                .thenReturn(null);

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(jwtService)
                .extrairEmail(token);

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarQuandoTokenForInvalido()
            throws ServletException, IOException {

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token))
                .thenReturn("teste@email.com");

        when(userDetailsService.loadUserByUsername("teste@email.com"))
                .thenReturn(userDetails);

        when(jwtService.tokenValido(token))
                .thenReturn(false);

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
        );

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void naoDeveCarregarUsuarioQuandoJaExistirAutenticacao()
            throws ServletException, IOException {

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

        when(jwtService.extrairEmail(token))
                .thenReturn("teste@email.com");

        jwtFilter.doFilterInternal(
                request,
                response,
                filterChain
        );

        verify(jwtService)
                .extrairEmail(token);

        verifyNoInteractions(userDetailsService);

        verify(filterChain)
                .doFilter(request, response);
    }

    @Test
    void deveDefinirRoleNoTokenDeAutenticacao()
            throws ServletException, IOException {

        String token = "jwt-token";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token)).thenReturn("teste@email.com");
        when(userDetailsService.loadUserByUsername("teste@email.com")).thenReturn(userDetails);
        when(jwtService.tokenValido(token)).thenReturn(true);
        when(jwtService.extrairRole(token)).thenReturn("ADMIN");

        jwtFilter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void shouldNotFilterDeveRetornarTrueParaSwaggerUi()
            throws ServletException {

        request.setRequestURI("/swagger-ui/index.html");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterDeveRetornarTrueParaApiDocs()
            throws ServletException {

        request.setRequestURI("/v3/api-docs/swagger-config");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterDeveRetornarTrueParaSwaggerHtml()
            throws ServletException {

        request.setRequestURI("/swagger-ui.html");
        assertTrue(jwtFilter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilterDeveRetornarFalseParaOutrasRotas()
            throws ServletException {

        request.setRequestURI("/api/clientes");
        assertFalse(jwtFilter.shouldNotFilter(request));
    }
}
