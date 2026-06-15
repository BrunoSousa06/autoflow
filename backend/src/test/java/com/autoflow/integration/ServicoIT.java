package com.autoflow.integration;

import com.autoflow.integration.config.AbstractIT;
import com.autoflow.integration.utils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Serviço - Testes de Integração")
class ServicoIT extends AbstractIT {

    private String adminToken;
    private String atendenteToken;
    private String clienteToken;

    @BeforeEach
    void configurar() {
        limparBancoDeDados();
        adminToken     = registrarELogar(TestUtils.EMAIL_ADMIN,     TestUtils.CPF_ATENDENTE, "ADMIN");
        atendenteToken = registrarELogar(TestUtils.EMAIL_ATENDENTE, TestUtils.CPF_MECANICO,  "ATENDENTE");
        clienteToken   = registrarELogar(TestUtils.EMAIL_CLIENTE,   TestUtils.CPF_CLIENTE,   "CLIENTE");
    }

    @Test
    @DisplayName("deve cadastrar serviço com sucesso")
    void deveCadastrarServico() {
        var body = TestUtils.servicoRequest("Troca de Óleo", "Troca completa do óleo do motor", 150.00);

        ResponseEntity<String> response = post("/servicos", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("nome").asText()).isEqualTo("Troca de Óleo");
        assertThat(json.get("valor").decimalValue()).isEqualByComparingTo("150.00");
    }

    @Test
    @DisplayName("deve listar serviços cadastrados")
    void deveListarServicos() {
        post("/servicos", TestUtils.servicoRequest("Alinhamento", "Alinhamento de rodas", 120.00), adminToken);

        ResponseEntity<String> response = get("/servicos", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("content").isArray()).isTrue();
        assertThat(json.get("content").size()).isGreaterThanOrEqualTo(1);
        assertThat(json.get("page").get("totalElements").asLong()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("deve buscar serviço por ID")
    void deveBuscarServicoPorId() {
        ResponseEntity<String> criado = post("/servicos",
                TestUtils.servicoRequest("Balanceamento", "Balanceamento de rodas", 80.00), adminToken);
        Long servicoId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = get("/servicos/" + servicoId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).get("id").asLong()).isEqualTo(servicoId);
    }

    @Test
    @DisplayName("deve atualizar serviço com sucesso")
    void deveAtualizarServico() {
        ResponseEntity<String> criado = post("/servicos",
                TestUtils.servicoRequest("Revisão Geral", "Revisão completa", 500.00), adminToken);
        long servicoId = parseJson(criado.getBody()).get("id").asLong();

        var atualizacao = TestUtils.servicoRequest("Revisão Geral Premium", "Revisão completa premium", 750.00);
        ResponseEntity<String> response = patch("/servicos/" + servicoId + "/atualizacao", atualizacao, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("nome").asText()).isEqualTo("Revisão Geral Premium");
        assertThat(json.get("valor").decimalValue()).isEqualByComparingTo("750.00");
    }

    @Test
    @DisplayName("deve deletar serviço com sucesso")
    void deveDeletarServico() {
        ResponseEntity<String> criado = post("/servicos",
                TestUtils.servicoRequest("Limpeza do Bico", "Limpeza de injetores", 200.00), adminToken);
        long servicoId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = delete("/servicos/" + servicoId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deve retornar 404 para serviço inexistente")
    void deveRetornar404ServicoInexistente() {
        ResponseEntity<String> response = get("/servicos/999999", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deve retornar 409 ao cadastrar nome duplicado")
    void deveRetornar409NomeDuplicado() {
        post("/servicos", TestUtils.servicoRequest("Troca de Filtro", "Filtro de ar", 60.00), adminToken);

        ResponseEntity<String> response = post("/servicos",
                TestUtils.servicoRequest("Troca de Filtro", "Filtro de óleo", 70.00), adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("deve retornar 403 quando cliente tenta cadastrar serviço")
    void deveRetornar403QuandoClienteCadastraServico() {
        ResponseEntity<String> response = post("/servicos",
                TestUtils.servicoRequest("Polimento", "Polimento externo", 300.00), clienteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("deve retornar 403 quando atendente tenta deletar serviço")
    void deveRetornar403QuandoAtendenteDeletaServico() {
        ResponseEntity<String> criado = post("/servicos",
                TestUtils.servicoRequest("Higienização", "Higienização interna", 180.00), adminToken);
        long servicoId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = delete("/servicos/" + servicoId, atendenteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("admin deve consultar tempo médio por serviço")
    void adminDeveConsultarTempoMedioPorServico() {
        ResponseEntity<String> response = get("/servicos/metricas/tempo-medio", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).isArray()).isTrue();
    }

    @Test
    @DisplayName("cliente não deve acessar métricas de tempo médio de serviços")
    void clienteNaoDeveAcessarMetricasServico() {
        ResponseEntity<String> response = get("/servicos/metricas/tempo-medio", clienteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
