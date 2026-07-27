package com.autoflow.config.security.service;

import com.autoflow.infrastructure.persistence.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "1234567890123456789012345678901234567890123456789012345678901234";

    @BeforeEach
    void setup() {

        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                SECRET
        );

        ReflectionTestUtils.setField(
                jwtService,
                "expiration",
                3600000L
        );
    }

    @Test
    void deveGerarToken() {

        String token =
                jwtService.gerarToken(
                        "admin@email.com",
                        "ROLE_ADMIN"
                );

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deveExtrairEmailDoToken() {

        String token =
                jwtService.gerarToken(
                        "admin@email.com",
                        "ROLE_ADMIN"
                );

        String email =
                jwtService.extrairEmail(token);

        assertEquals(
                "admin@email.com",
                email
        );
    }

    @Test
    void deveExtrairRoleDoToken() {

        String token =
                jwtService.gerarToken(
                        "admin@email.com",
                        "ROLE_ADMIN"
                );

        String role =
                jwtService.extrairRole(token);

        assertEquals(
                "ROLE_ADMIN",
                role
        );
    }

    @Test
    void deveRetornarTrueQuandoTokenForValido() {

        String token =
                jwtService.gerarToken(
                        "admin@email.com",
                        "ROLE_ADMIN"
                );

        assertTrue(
                jwtService.tokenValido(token)
        );
    }

    @Test
    void deveRetornarFalseQuandoTokenForInvalido() {

        String token = "token-invalido";

        assertFalse(
                jwtService.tokenValido(token)
        );
    }

    @Test
    void deveGerarTokenComEmailCorreto() {

        String token =
                jwtService.gerarToken(
                        "usuario@email.com",
                        "ROLE_CLIENTE"
                );

        assertEquals(
                "usuario@email.com",
                jwtService.extrairEmail(token)
        );
    }

    @Test
    void deveGerarTokenComRoleCorreta() {

        String token =
                jwtService.gerarToken(
                        "usuario@email.com",
                        "ROLE_CLIENTE"
                );

        assertEquals(
                "ROLE_CLIENTE",
                jwtService.extrairRole(token)
        );
    }
}


