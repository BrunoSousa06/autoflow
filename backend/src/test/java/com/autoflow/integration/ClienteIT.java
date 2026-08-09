package com.autoflow.integration;

import com.autoflow.integration.config.AbstractIT;
import com.autoflow.integration.utils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cliente - Testes de Integração")
class ClienteIT extends AbstractIT {

    private String adminToken;
    private String mecanicoToken;
    private String clienteToken;

    @BeforeEach
    void configurar() {
        limparBancoDeDados();
        adminToken    = registrarELogar(TestUtils.EMAIL_ADMIN,    TestUtils.CPF_ATENDENTE, "ADMIN");
        mecanicoToken = registrarELogar(TestUtils.EMAIL_MECANICO, TestUtils.CPF_MECANICO,  "MECANICO");

        registrarELogar(TestUtils.EMAIL_CLIENTE, TestUtils.CPF_CLIENTE, "CLIENTE");
        clienteToken  = logar(TestUtils.EMAIL_CLIENTE);
    }

    private String logar(String email) {
        var resp = restTemplate.postForEntity("/auth/login",
                jsonEntity(TestUtils.loginRequest(email)), String.class);
        return extrairCampo(resp.getBody(), "token");
    }

    @Test
    @DisplayName("deve cadastrar cliente com sucesso")
    void deveCadastrarCliente() {
        var body = TestUtils.clienteRequest("Ana Lima", TestUtils.CPF_CLIENTE_2, "ana@test.com");

        ResponseEntity<String> response = post("/clientes", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("nome").asText()).isEqualTo("Ana Lima");
        assertThat(json.get("cpfCnpj").asText()).isEqualTo(TestUtils.CPF_CLIENTE_2);
        assertThat(json.get("email").asText()).isEqualTo("ana@test.com");
    }

    @Test
    @DisplayName("deve listar todos os clientes")
    void deveListarClientes() {
        post("/clientes", TestUtils.clienteRequest("Carlos", TestUtils.CPF_CLIENTE_2, "carlos@test.com"), adminToken);

        ResponseEntity<String> response = get("/clientes", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("deve buscar cliente por CPF")
    void deveBuscarClientePorCpf() {
        post("/clientes", TestUtils.clienteRequest("Beatriz", TestUtils.CPF_CLIENTE_2, "bea@test.com"), adminToken);

        ResponseEntity<String> response = get("/clientes/" + TestUtils.CPF_CLIENTE_2, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("cpfCnpj").asText()).isEqualTo(TestUtils.CPF_CLIENTE_2);
    }

    @Test
    @DisplayName("deve atualizar cliente e sincronizar nome e email do usuario associado")
    void deveAtualizarClienteESincronizarUsuarioAssociado() {
        Long clienteId = parseJson(get("/clientes/me", clienteToken).getBody()).get("id").asLong();
        var atualizacao = TestUtils.clienteRequest(
                "Usuario Atualizado", TestUtils.CPF_CLIENTE, "usuario.atualizado@test.com");

        ResponseEntity<String> response = patch("/clientes/" + clienteId + "/atualizacao", atualizacao, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode clienteAtualizado = parseJson(response.getBody());
        assertThat(clienteAtualizado.get("nome").asText()).isEqualTo("Usuario Atualizado");
        assertThat(clienteAtualizado.get("email").asText()).isEqualTo("usuario.atualizado@test.com");

        var usuario = jdbcTemplate.queryForMap("""
                SELECT u.nome AS usuario_nome, u.email AS usuario_email
                FROM usuarios u
                JOIN clientes c ON c.usuario_id = u.id
                WHERE c.id = ?
                """, clienteId);

        assertThat(usuario)
                .containsEntry("usuario_nome", "Usuario Atualizado")
                .containsEntry("usuario_email", "usuario.atualizado@test.com");
    }

    @Test
    @DisplayName("deve deletar cliente com sucesso")
    void deveDeletarCliente() {
        ResponseEntity<String> criado = post("/clientes", TestUtils.clienteRequest("Lucas", TestUtils.CPF_CLIENTE_2, "lucas@test.com"), adminToken);
        Long clienteId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = delete("/clientes/" + clienteId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("cliente deletado com sucesso");
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
        post("/clientes", TestUtils.clienteRequest("Fernanda", TestUtils.CPF_CLIENTE_2, "fernanda@test.com"), adminToken);

        var duplicado = TestUtils.clienteRequest("Fernanda 2", TestUtils.CPF_CLIENTE_2, "fernanda2@test.com");
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
    @DisplayName("deve retornar os dados do próprio cliente autenticado via /me")
    void deveRetornarMeuPerfil() {
        ResponseEntity<String> response = get("/clientes/me", clienteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("cpfCnpj").asText()).isEqualTo(TestUtils.CPF_CLIENTE);
        assertThat(json.get("email").asText()).isEqualTo(TestUtils.EMAIL_CLIENTE);
    }

    @Test
    @DisplayName("deve retornar 403 quando ADMIN tenta acessar /me")
    void deveRetornar403QuandoAdminAcessaMeuPerfil() {
        ResponseEntity<String> response = get("/clientes/me", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("deve retornar 404 quando CLIENTE não tem cadastro vinculado ao usuário")
    void deveRetornar404QuandoClienteSemCadastro() {
        String emailSemCadastro = TestUtils.emailUnico();
        registrarELogar(emailSemCadastro, TestUtils.CPF_CLIENTE_2, "CLIENTE");
        String semCadastroToken = logar(emailSemCadastro);
        jdbcTemplate.update("DELETE FROM clientes WHERE cpf_cnpj = ?", TestUtils.CPF_CLIENTE_2);

        // Não criamos ClienteEntity para este usuário
        ResponseEntity<String> response = get("/clientes/me", semCadastroToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deve retornar 404 ao tentar atualizar cliente com ID inexistente")
    void deveRetornar404AoAtualizarClienteInexistente() {
        var body = TestUtils.clienteRequest("Fantasma", TestUtils.CPF_CLIENTE, "fantasma@test.com");

        ResponseEntity<String> response = patch("/clientes/99999/atualizacao", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
