package com.autoflow.integration;

import com.autoflow.integration.config.AbstractIT;
import com.autoflow.integration.utils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Peça/Insumo - Testes de Integração")
class PecaInsumoIT extends AbstractIT {

    private String adminToken;
    private String clienteToken;

    @BeforeEach
    void configurar() {
        limparBancoDeDados();
        adminToken   = registrarELogar(TestUtils.EMAIL_ADMIN,   TestUtils.CPF_ATENDENTE, "ADMIN");
        clienteToken = registrarELogar(TestUtils.EMAIL_CLIENTE, TestUtils.CPF_CLIENTE,   "CLIENTE");
    }

    @Test
    @DisplayName("deve cadastrar peça com sucesso")
    void deveCadastrarPeca() {
        var body = TestUtils.pecaRequest("Filtro de Óleo Premium", 50, 35.90, "PECA");

        ResponseEntity<String> response = post("/peca-insumo", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("nome").asText()).isEqualTo("Filtro de Óleo Premium");
        assertThat(json.get("quantidade").asInt()).isEqualTo(50);
        assertThat(json.get("tipo").asText()).isEqualTo("PECA");
    }

    @Test
    @DisplayName("deve cadastrar insumo com sucesso")
    void deveCadastrarInsumo() {
        var body = TestUtils.pecaRequest("Óleo Motor 5W30", 100, 45.00, "INSUMO");

        ResponseEntity<String> response = post("/peca-insumo", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(parseJson(response.getBody()).get("tipo").asText()).isEqualTo("INSUMO");
    }

    @Test
    @DisplayName("deve listar peças e insumos paginado")
    void deveListarPecas() {
        post("/peca-insumo", TestUtils.pecaRequest("Vela de Ignição", 30, 25.00, "PECA"), adminToken);

        ResponseEntity<String> response = get("/peca-insumo", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.has("content")).isTrue();
        assertThat(json.get("content").isArray()).isTrue();
        assertThat(json.get("content").size()).isGreaterThanOrEqualTo(1);
        assertThat(json.get("page").get("totalElements").asInt()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("deve buscar peça por ID")
    void deveBuscarPecaPorId() {
        ResponseEntity<String> criado = post("/peca-insumo",
                TestUtils.pecaRequest("Correia Dentada", 20, 180.00, "PECA"), adminToken);
        Long pecaId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = get("/peca-insumo/" + pecaId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).get("id").asLong()).isEqualTo(pecaId);
    }

    @Test
    @DisplayName("deve atualizar peça com sucesso")
    void deveAtualizarPeca() {
        ResponseEntity<String> criado = post("/peca-insumo",
                TestUtils.pecaRequest("Pastilha de Freio", 40, 95.00, "PECA"), adminToken);
        Long pecaId = parseJson(criado.getBody()).get("id").asLong();

        var atualizacao = TestUtils.pecaRequest("Pastilha de Freio Premium", 45, 120.00, "PECA");
        ResponseEntity<String> response = patch("/peca-insumo/" + pecaId + "/atualizacao", atualizacao, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("nome").asText()).isEqualTo("Pastilha de Freio Premium");
        assertThat(json.get("quantidade").asInt()).isEqualTo(45);
    }

    @Test
    @DisplayName("deve deletar peça com sucesso")
    void deveDeletarPeca() {
        ResponseEntity<String> criado = post("/peca-insumo",
                TestUtils.pecaRequest("Disco de Freio", 15, 220.00, "PECA"), adminToken);
        Long pecaId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = delete("/peca-insumo/" + pecaId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deve retornar 404 para peça inexistente")
    void deveRetornar404PecaInexistente() {
        ResponseEntity<String> response = get("/peca-insumo/999999", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deve retornar 400 ao cadastrar nome duplicado (BAD_REQUEST conforme implementação do serviço)")
    void deveRetornar400NomeDuplicado() {
        post("/peca-insumo", TestUtils.pecaRequest("Amortecedor Dianteiro", 10, 350.00, "PECA"), adminToken);

        ResponseEntity<String> response = post("/peca-insumo",
                TestUtils.pecaRequest("Amortecedor Dianteiro", 5, 380.00, "PECA"), adminToken);

        // PecaInsumoService lança ResponseStatusException(BAD_REQUEST) para nomes duplicados
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("deve retornar 403 quando cliente tenta cadastrar peça")
    void deveRetornar403QuandoClienteCadastraPeca() {
        ResponseEntity<String> response = post("/peca-insumo",
                TestUtils.pecaRequest("Pneu Remold", 5, 200.00, "PECA"), clienteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("deve retornar 400 ao omitir campo 'tipo' obrigatório")
    void deveRetornar400SemTipo() {
        // PecaInsumoRequest tem @NotNull no campo tipo - sem tipo a validação falha
        var body = java.util.Map.of("nome", "Peca Sem Tipo", "quantidade", 5, "valor", 50.00);

        ResponseEntity<String> response = post("/peca-insumo", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}