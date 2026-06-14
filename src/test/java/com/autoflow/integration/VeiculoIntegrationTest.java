package com.autoflow.integration;

import com.autoflow.integration.config.AbstractIntegrationTest;
import com.autoflow.integration.utils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Veículo - Testes de Integração")
class VeiculoIntegrationTest extends AbstractIntegrationTest {

    private String adminToken;
    private String placa;

    @BeforeEach
    void configurar() {
        limparBancoDeDados();
        adminToken = registrarELogar(TestUtils.EMAIL_ADMIN, TestUtils.CPF_ATENDENTE, "ADMIN");
        placa = TestUtils.placaUnica();

        // cliente precisa existir para associar veículo
        post("/clientes", TestUtils.clienteRequest("Dono do Carro", TestUtils.CPF_CLIENTE, "dono@test.com"), adminToken);
    }

    @Test
    @DisplayName("deve cadastrar veículo com sucesso")
    void deveCadastrarVeiculo() {
        var body = TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, placa, "Honda", "Civic", 2022);

        ResponseEntity<String> response = post("/veiculos", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.get("placa").asText()).isEqualTo(placa);
        assertThat(json.get("marca").asText()).isEqualTo("Honda");
    }

    @Test
    @DisplayName("deve listar todos os veículos")
    void deveListarVeiculos() {
        post("/veiculos", TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, placa, "Ford", "Ka", 2019), adminToken);

        ResponseEntity<String> response = get("/veiculos", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode json = parseJson(response.getBody());
        assertThat(json.isArray()).isTrue();
        assertThat(json.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("deve buscar veículo por ID")
    void deveBuscarVeiculo() {
        ResponseEntity<String> criado = post("/veiculos",
                TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, placa, "VW", "Gol", 2018), adminToken);
        Long veiculoId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = get("/veiculos/" + veiculoId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).get("id").asLong()).isEqualTo(veiculoId);
    }

    @Test
    @DisplayName("deve atualizar veículo com sucesso")
    void deveAtualizarVeiculo() {
        ResponseEntity<String> criado = post("/veiculos",
                TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, placa, "Fiat", "Uno", 2015), adminToken);
        Long veiculoId = parseJson(criado.getBody()).get("id").asLong();

        String novaPlaca = TestUtils.placaUnica();
        var atualizacao = TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, novaPlaca, "Fiat", "Uno Atualizado", 2015);

        ResponseEntity<String> response = patch("/veiculos/" + veiculoId + "/atualizacao", atualizacao, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(response.getBody()).get("modelo").asText()).isEqualTo("Uno Atualizado");
    }

    @Test
    @DisplayName("deve deletar veículo com sucesso")
    void deveDeletarVeiculo() {
        ResponseEntity<String> criado = post("/veiculos",
                TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, placa, "Chev", "Onix", 2021), adminToken);
        Long veiculoId = parseJson(criado.getBody()).get("id").asLong();

        ResponseEntity<String> response = delete("/veiculos/" + veiculoId, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("deve retornar 404 para veículo inexistente")
    void deveRetornar404VeiculoInexistente() {
        ResponseEntity<String> response = get("/veiculos/999999", adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deve retornar 409 ao cadastrar placa duplicada")
    void deveRetornar409PlacaDuplicada() {
        post("/veiculos", TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, placa, "Hyundai", "HB20", 2020), adminToken);

        ResponseEntity<String> response = post("/veiculos",
                TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, placa, "Kia", "Sportage", 2021), adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    @DisplayName("deve retornar 404 ao cadastrar veículo para cliente inexistente")
    void deveRetornar404ClienteInexistente() {
        Map<String, Object> bodyComCpfInexistente = Map.of(
                "cpfCnpj", "98765432100",
                "placa", placa,
                "marca", "BMW",
                "modelo", "X5",
                "ano", 2023
        );

        ResponseEntity<String> response = post("/veiculos", bodyComCpfInexistente, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deve retornar 400 com formato de placa inválido")
    void deveRetornar400PlacaInvalida() {
        var body = TestUtils.veiculoRequest(TestUtils.CPF_CLIENTE, "PLACA-INVALIDA", "Toyota", "Corolla", 2022);

        ResponseEntity<String> response = post("/veiculos", body, adminToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}