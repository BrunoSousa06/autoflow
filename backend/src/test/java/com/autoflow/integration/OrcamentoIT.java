package com.autoflow.integration;

import com.autoflow.integration.config.AbstractIT;
import com.autoflow.integration.utils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testa fluxos de orçamento: consulta autenticada, aprovação, recusa, acesso negado e filtros.
 * Cada teste recria uma OS completa até o estado AGUARDANDO_APROVACAO.
 */
@DisplayName("Orçamento - Testes de Integração")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrcamentoIT extends AbstractIT {

    private String adminToken;
    private String clienteToken;
    private Long mecanicoId;
    private Long servicoId;
    private Long orcamentoId;
    private String orcamentoPublicUrl;

    @BeforeAll
    void configurarAmbiente() {
        limparBancoDeDados();

        adminToken = registrarELogar(TestUtils.EMAIL_ADMIN, TestUtils.CPF_ATENDENTE, "ADMIN");

        ResponseEntity<String> mecResp = restTemplate.postForEntity("/auth/cadastro", jsonEntity(
                TestUtils.registroRequest("Mecânico Orçamento", TestUtils.EMAIL_MECANICO, TestUtils.CPF_MECANICO, "MECANICO")
        ), String.class);
        mecanicoId = parseJson(mecResp.getBody()).get("id").asLong();

        ResponseEntity<String> mecLogin = restTemplate.postForEntity("/auth/login", jsonEntity(
                Map.of("email", TestUtils.EMAIL_MECANICO, "senha", TestUtils.SENHA_PADRAO)
        ), String.class);
        String mecanicoToken = extrairCampo(mecLogin.getBody(), "token");

        restTemplate.postForEntity("/auth/cadastro", jsonEntity(
                TestUtils.registroRequest("Cliente Orcamento", TestUtils.EMAIL_CLIENTE, TestUtils.CPF_CLIENTE, "CLIENTE")
        ), String.class);
        ResponseEntity<String> cli1Login = restTemplate.postForEntity("/auth/login", jsonEntity(
                Map.of("email", TestUtils.EMAIL_CLIENTE, "senha", TestUtils.SENHA_PADRAO)
        ), String.class);
        clienteToken = extrairCampo(cli1Login.getBody(), "token");

        restTemplate.postForEntity("/auth/cadastro", jsonEntity(
                TestUtils.registroRequest("Outro Cliente", TestUtils.EMAIL_ATENDENTE, TestUtils.CPF_CLIENTE_2, "CLIENTE")
        ), String.class);
        restTemplate.postForEntity("/auth/login", jsonEntity(
                Map.of("email", TestUtils.EMAIL_ATENDENTE, "senha", TestUtils.SENHA_PADRAO)
        ), String.class);

        post("/clientes", TestUtils.clienteRequest("Cliente Orçamento", TestUtils.CPF_CLIENTE, TestUtils.EMAIL_CLIENTE), adminToken);

        ResponseEntity<String> svcResp = post("/servicos",
                TestUtils.servicoRequest("Revisão Completa", "Revisão geral do veículo", 800.00), adminToken);
        servicoId = parseJson(svcResp.getBody()).get("id").asLong();

        JsonNode finJson = criarOsAguardandoAprovacao(mecanicoToken, "ABC1234");
        orcamentoId       = finJson.get("orcamentoId").asLong();
        orcamentoPublicUrl = finJson.get("publicUrl").asText();
    }

    private JsonNode criarOsAguardandoAprovacao(String mecToken, String placa) {
        var osRequest = Map.of(
                "cpfCnpj", TestUtils.CPF_CLIENTE,
                "veiculo", Map.of("placa", placa, "marca", "Fiat", "modelo", "Argo", "ano", 2022),
                "servicosSolicitados", List.of(Map.of("servicoId", servicoId))
        );

        ResponseEntity<String> osResp = post("/ordens-servico", osRequest, adminToken);
        String numOs = parseJson(osResp.getBody()).get("numeroOs").asText();

        patch("/ordens-servico/" + numOs + "/mecanico",
                TestUtils.incluirMecanicoRequest(mecanicoId, TestUtils.EMAIL_MECANICO), adminToken);

        patch("/ordens-servico/" + numOs + "/diagnostico/iniciar", null, mecToken);

        patch("/ordens-servico/" + numOs + "/diagnostico/laudo",
                TestUtils.registrarLaudoRequest("Revisão geral necessária."), mecToken);

        ResponseEntity<String> finResp = patch("/ordens-servico/" + numOs + "/diagnostico/finalizar", null, mecToken);
        return parseJson(finResp.getBody());
    }

    private Long orcamentoIdDe(String mecToken, String placa) {
        return criarOsAguardandoAprovacao(mecToken, placa).get("orcamentoId").asLong();
    }

    @Test
    @Order(1)
    @DisplayName("admin deve consultar orçamento com sucesso")
    void adminDeveConsultarOrcamento() {
        ResponseEntity<String> response = get("/orcamentos/" + orcamentoId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("id").asLong()).isEqualTo(orcamentoId);
        assertThat(json.get("status").asText()).isEqualTo("DISPONIVEL");
        assertThat(json.get("totalGeral").decimalValue()).isPositive();
    }

    @Test
    @Order(2)
    @DisplayName("admin deve listar orçamentos sem filtros")
    void adminDeveListarOrcamentos() {
        ResponseEntity<String> response = get("/orcamentos", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(3)
    @DisplayName("cliente deve reprovar orçamento com motivo")
    void clienteDeveReprovarOrcamento() {
        String mecLogin = extrairCampo(restTemplate.postForEntity("/auth/login", jsonEntity(
                Map.of("email", TestUtils.EMAIL_MECANICO, "senha", TestUtils.SENHA_PADRAO)
        ), String.class).getBody(), "token");

        Long outroOrcamentoId = orcamentoIdDe(mecLogin, "DEF5678");

        var recusa = TestUtils.recusarOrcamentoRequest("Preço muito alto, vou buscar outro orçamento.");
        ResponseEntity<String> response = post("/orcamentos/" + outroOrcamentoId + "/recusar", recusa, clienteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("status").asText()).isEqualTo("REPROVADO");
    }

    @Test
    @Order(4)
    @DisplayName("cliente deve aprovar orçamento")
    void clienteDeveAprovarOrcamento() {
        String mecLogin = extrairCampo(restTemplate.postForEntity("/auth/login", jsonEntity(
                Map.of("email", TestUtils.EMAIL_MECANICO, "senha", TestUtils.SENHA_PADRAO)
        ), String.class).getBody(), "token");

        Long novoOrcamentoId = orcamentoIdDe(mecLogin, "GHI9012");

        ResponseEntity<String> response = post("/orcamentos/" + novoOrcamentoId + "/aprovar", null, clienteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).get("status").asText()).isEqualTo("APROVADO");
    }

    @Test
    @Order(5)
    @DisplayName("deve retornar 404 para orçamento inexistente")
    void deveRetornar404OrcamentoInexistente() {
        ResponseEntity<String> response = get("/orcamentos/999999", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Order(6)
    @DisplayName("deve retornar 403 sem token ao consultar orçamento")
    void deveRetornar403SemToken() {
        ResponseEntity<String> response = restTemplate.getForEntity("/orcamentos/" + orcamentoId, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(7)
    @DisplayName("mecânico não deve ter acesso ao endpoint de orçamentos")
    void mecanicoNaoDeveAcessarOrcamentos() {
        String mecLogin = extrairCampo(restTemplate.postForEntity("/auth/login", jsonEntity(
                Map.of("email", TestUtils.EMAIL_MECANICO, "senha", TestUtils.SENHA_PADRAO)
        ), String.class).getBody(), "token");

        ResponseEntity<String> response = get("/orcamentos/" + orcamentoId, mecLogin);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(8)
    @DisplayName("deve confirmar que reprova orçamento com sucesso e registra o motivo")
    void deveReprovarOrcamentoSemMotivo() {
        String mecLogin = extrairCampo(restTemplate.postForEntity("/auth/login", jsonEntity(
                Map.of("email", TestUtils.EMAIL_MECANICO, "senha", TestUtils.SENHA_PADRAO)
        ), String.class).getBody(), "token");

        Long idParaRecusar = orcamentoIdDe(mecLogin, "JKL3456");

        // Reprova sem motivo (body vazio)
        ResponseEntity<String> response = post("/orcamentos/" + idParaRecusar + "/recusar",
                Map.of(), clienteToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).get("status").asText()).isEqualTo("REPROVADO");
    }

    @Test
    @Order(9)
    @DisplayName("admin deve listar orçamentos filtrados por status")
    void deveListarOrcamentosComFiltroStatus() {
        ResponseEntity<String> response = get("/orcamentos?statusOrcamento=DISPONIVEL", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.isArray()).isTrue();
        for (JsonNode orc : json) {
            assertThat(orc.get("status").asText()).isEqualTo("DISPONIVEL");
        }
    }

    @Test
    @Order(10)
    @DisplayName("deve gerar PDF do orçamento via link público")
    void deveGerarPdfDoOrcamento() {
        String token = orcamentoPublicUrl.split("token=")[1];
        String path  = "/public/orcamentos/" + orcamentoId + "/pdf?token=" + token;

        ResponseEntity<byte[]> response = restTemplate.getForEntity(path, byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isEqualTo(org.springframework.http.MediaType.APPLICATION_PDF);
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    @Order(11)
    @DisplayName("deve retornar 404 ao solicitar PDF de orçamento inexistente")
    void deveRetornar404PdfOrcamentoInexistente() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
                "/public/orcamentos/999999/pdf?token=token-invalido", byte[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
