package com.autoflow.integration;

import com.autoflow.integration.config.AbstractIntegrationTest;
import com.autoflow.integration.utils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cliente - Testes de Integração")
class ClienteI
        ntegrationTest extends AbstractIntegrationTest {

    private String adminToken;
    private String mecanicoToken;

    @BeforeEach
    void configurar() {
        limparBancoDeDados();
        adminToken    = registrarELogar(TestUtils.EMAIL_ADMIN,    TestUtils.CPF_ATENDENTE, "ADMIN");
        mecanicoToken = registrarELogar(TestUtils.EMAIL_MECANICO, TestUtils.CPF_MECANICO,  "MECANICO");
    }

    @Test
    @DisplayName("deve cadastrar cliente com sucesso")
    void deveCadastrarCliente() {
        var body = TestUtils.clienteRequest("Ana Lima", TestUtils.CPF_CLIENTE, "ana@test.com");

        ResponseEntity<String> response = post("/clientes", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("nome").asText()).isEqualTo("Ana Lima");
        assertThat(json.get("cpfCnpj").asText()).isEqualTo(TestUtils.CPF_CLIENTE);
        assertThat(json.get("email").asText()).isEqualTo("ana@test.com");
    }

    @Test
    @DisplayName("deve listar todos os clientes")
    void deveListarClientes() {
        post("/clientes", TestUtils.clienteRequest("Carlos", TestUtils.CPF_CLIENTE, "carlos@test.com"), adminToken);

        ResponseEntity<String> response = get("/clientes", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("deve buscar cliente por CPF")
    void deveBuscarClientePorCpf() {
        post("/clientes", TestUtils.clienteRequest("Beatriz", TestUtils.CPF_CLIENTE, "bea@test.com"), adminToken);

        ResponseEntity<String> response = get("/clientes/" + TestUtils.CPF_CLIENTE, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("cpfCnpj").asText()).isEqualTo(TestUtils.CPF_CLIENTE);
    }

    @Test
    @DisplayName("deve atualizar dados do cliente")
    void deveAtualizarCliente() {
        ResponseEntity<String> criado = post("/clientes", TestUtils.clienteRequest("Pedro", TestUtils.CPF_CLIENTE, "pedro@test.com"), adminToken);
        Long clienteId = parseJson(criado.getBody()).get("id").asLong();

        var atualizacao = TestUtils.clienteRequest("Pedro Atualizado", TestUtils.CPF_CLIENTE, "pedro@test.com");
        ResponseEntity<String> response = patch("/clientes/" + clienteId + "/atualizacao", atualizacao, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).get("nome").asText()).isEqualTo("Pedro Atualizado");
    }

    @Test
    @DisplayName("deve deletar cliente com sucesso")
    void deveDeletarCliente() {
        ResponseEntity<String> criado = post("/clientes", TestUtils.clienteRequest("Lucas", TestUtils.CPF_CLIENTE, "lucas@test.com"), adminToken);
        Long clienteId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = delete("/clientes/" + clienteId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deve retornar 404 para cliente inexistente")
    void deveRetornar404ClienteInexistente() {
        ResponseEntity<String> response = get("/clientes/99999", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deve retornar 409 ao cadastrar CPF duplicado")
    void deveRetornar409CpfDuplicado() {
        post("/clientes", TestUtils.clienteRequest("Fernanda", TestUtils.CPF_CLIENTE, "fernanda@test.com"), adminToken);

        var duplicado = TestUtils.clienteRequest("Fernanda 2", TestUtils.CPF_CLIENTE, "fernanda2@test.com");
        ResponseEntity<String> response = post("/clientes", duplicado, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("deve retornar 403 quando mecânico tenta listar clientes")
    void deveRetornar403QuandoMecanicoListaClientes() {
        ResponseEntity<String> response = get("/clientes", mecanicoToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("deve retornar 403 sem token de autenticação")
    void deveRetornar403SemToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/clientes", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("deve retornar 404 ao tentar atualizar cliente com ID inexistente")
    void deveRetornar404AoAtualizarClienteInexistente() {
        var body = TestUtils.clienteRequest("Fantasma", TestUtils.CPF_CLIENTE, "fantasma@test.com");

        ResponseEntity<String> response = patch("/clientes/99999/atualizacao", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}