package com.autoflow.integration;

import com.autoflow.integration.config.AbstractIT;
import com.autoflow.integration.utils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuários administrativos - Testes de Integração")
class UsuarioAdminIT extends AbstractIT {

    private String adminToken;
    private String atendenteToken;
    private String clienteToken;

    @BeforeEach
    void configurar() {
        limparBancoDeDados();
        adminToken = registrarELogar(TestUtils.EMAIL_ADMIN, TestUtils.CPF_ATENDENTE, "ADMIN");
        atendenteToken = registrarELogar(TestUtils.EMAIL_ATENDENTE, TestUtils.CPF_MECANICO, "ATENDENTE");
        clienteToken = registrarELogar(TestUtils.EMAIL_CLIENTE, TestUtils.CPF_CLIENTE, "CLIENTE");
    }

    @Test
    @DisplayName("admin deve listar todos os usuários pelo endpoint administrativo")
    void adminDeveListarTodosOsUsuarios() {
        ResponseEntity<String> response = get("/usuarios", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = parseJson(response.getBody());
        assertThat(body.isArray()).isTrue();
        assertThat(body.size()).isEqualTo(3);
        assertThat(body.findValuesAsText("role"))
                .containsExactlyInAnyOrder("ADMIN", "ATENDENTE", "CLIENTE");
    }

    @Test
    @DisplayName("atendente deve listar usuários pelo endpoint administrativo")
    void atendenteDeveListarUsuarios() {
        ResponseEntity<String> response = get("/usuarios", atendenteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).size()).isEqualTo(3);
    }

    @Test
    @DisplayName("cliente não deve listar usuários pelo endpoint administrativo")
    void clienteNaoDeveListarUsuarios() {
        ResponseEntity<String> response = get("/usuarios", clienteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("admin deve cadastrar mecânico e persistir a role informada")
    void adminDeveCadastrarMecanico() {
        String email = TestUtils.emailUnico();
        ResponseEntity<String> response = post(
                "/usuarios",
                registroStaff(email, "MECANICO"),
                adminToken
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode body = parseJson(response.getBody());
        assertThat(body.get("email").asText()).isEqualTo(email);
        assertThat(body.get("role").asText()).isEqualTo("MECANICO");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT role FROM usuarios WHERE email = ?", String.class, email))
                .isEqualTo("MECANICO");
    }

    @Test
    @DisplayName("atendente não deve cadastrar mecânico")
    void atendenteNaoDeveCadastrarMecanico() {
        String email = TestUtils.emailUnico();
        ResponseEntity<String> response = post(
                "/usuarios",
                registroStaff(email, "MECANICO"),
                atendenteToken
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, email))
                .isZero();
    }

    private Map<String, Object> registroStaff(String email, String role) {
        return Map.of(
                "nome", "Staff Integracao",
                "email", email,
                "cpfCnpj", TestUtils.CPF_CLIENTE_2,
                "telefone", "11999999999",
                "senha", TestUtils.SENHA_PADRAO,
                "role", role
        );
    }
}
