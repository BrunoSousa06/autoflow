package com.autoflow.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void devePermitirTodasAsOrigensSemCredenciaisPorPadrao() {
        SecurityConfig securityConfig = new SecurityConfig(mock(JwtFilter.class));
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins", List.of("*"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/ordens-servico");
        CorsConfiguration cors = securityConfig.corsConfigurationSource()
                .getCorsConfiguration(request);

        assertThat(cors.getAllowedOrigins()).containsExactly("*");
        assertThat(cors.getAllowCredentials()).isFalse();
    }
}
