package com.autoflow.integration;

import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.integration.config.AbstractIT;
import com.autoflow.integration.utils.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrdemServicoListagemIT extends AbstractIT {

    private String adminToken;
    private Long servicoId;
    private String emExecucaoAntiga;
    private String emExecucaoRecente;
    private String aguardandoAprovacao;
    private String emDiagnostico;
    private String recebida;
    private String recebidaComMesmoInstante;
    private String finalizada;
    private String entregue;

    @BeforeEach
    void configurarDados() {
        limparBancoDeDados();

        adminToken = registrarELogar(TestUtils.EMAIL_ADMIN, TestUtils.CPF_ATENDENTE, "ADMIN");
        registrarELogar(TestUtils.EMAIL_CLIENTE, TestUtils.CPF_CLIENTE, "CLIENTE");
        post("/clientes", TestUtils.clienteRequest("Cliente Operacional", TestUtils.CPF_CLIENTE, "cliente.operacional@test.com"), adminToken);

        ResponseEntity<String> servicoResponse = post(
                "/servicos",
                TestUtils.servicoRequest("Servico operacional", "Servico para teste de fila", 100.00),
                adminToken);
        assertThat(servicoResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        servicoId = parseJson(servicoResponse.getBody()).get("id").asLong();

        emExecucaoAntiga = criarOrdem("OPR1001", StatusOrdemServico.EM_EXECUCAO, LocalDateTime.of(2025, 1, 1, 8, 0));
        emExecucaoRecente = criarOrdem("OPR1002", StatusOrdemServico.EM_EXECUCAO, LocalDateTime.of(2025, 2, 1, 8, 0));
        aguardandoAprovacao = criarOrdem("OPR1003", StatusOrdemServico.AGUARDANDO_APROVACAO, LocalDateTime.of(2024, 12, 1, 8, 0));
        emDiagnostico = criarOrdem("OPR1004", StatusOrdemServico.EM_DIAGNOSTICO, LocalDateTime.of(2024, 11, 1, 8, 0));
        recebida = criarOrdem("OPR1005", StatusOrdemServico.RECEBIDA, LocalDateTime.of(2024, 10, 1, 8, 0));
        recebidaComMesmoInstante = criarOrdem("OPR1006", StatusOrdemServico.RECEBIDA, LocalDateTime.of(2024, 10, 1, 8, 0));
        finalizada = criarOrdem("OPR1007", StatusOrdemServico.FINALIZADA, LocalDateTime.of(2024, 1, 1, 8, 0));
        entregue = criarOrdem("OPR1008", StatusOrdemServico.ENTREGUE, LocalDateTime.of(2024, 2, 1, 8, 0));
    }

    @Test
    void deveOrdenarFilaOperacionalEManterHistoricoPersistido() {
        JsonNode response = listar("/ordens-servico?page=0&size=20");

        assertThat(response.get("page").get("totalElements").asLong()).isEqualTo(6);
        assertThat(numeros(response.get("content"))).containsExactly(
                emExecucaoAntiga,
                emExecucaoRecente,
                aguardandoAprovacao,
                emDiagnostico,
                recebida,
                recebidaComMesmoInstante
        );
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ordem_servico WHERE numero_os IN (?, ?)",
                Integer.class,
                finalizada,
                entregue
        )).isEqualTo(2);
    }

    @Test
    void deveAplicarOrdenacaoAntesDaPaginacao() {
        JsonNode primeiraPagina = listar("/ordens-servico?page=0&size=2");
        JsonNode segundaPagina = listar("/ordens-servico?page=1&size=2");
        JsonNode terceiraPagina = listar("/ordens-servico?page=2&size=2");

        assertThat(numeros(primeiraPagina.get("content"))).containsExactly(emExecucaoAntiga, emExecucaoRecente);
        assertThat(numeros(segundaPagina.get("content"))).containsExactly(aguardandoAprovacao, emDiagnostico);
        assertThat(numeros(terceiraPagina.get("content"))).containsExactly(recebida, recebidaComMesmoInstante);
        assertThat(primeiraPagina.get("page").get("totalElements").asLong()).isEqualTo(6);
        assertThat(segundaPagina.get("page").get("totalElements").asLong()).isEqualTo(6);
        assertThat(terceiraPagina.get("page").get("totalElements").asLong()).isEqualTo(6);
    }

    @Test
    void deveCombinarFiltrosEExcluirStatusNaoOperacionais() {
        JsonNode recebidaFiltrada = listar("/ordens-servico?numeroOs=" + recebida + "&status=RECEBIDA");
        JsonNode finalizadaFiltrada = listar("/ordens-servico?status=FINALIZADA");
        JsonNode entregueFiltrada = listar("/ordens-servico?status=ENTREGUE");

        assertThat(numeros(recebidaFiltrada.get("content"))).containsExactly(recebida);
        assertThat(finalizadaFiltrada.get("page").get("totalElements").asLong()).isZero();
        assertThat(entregueFiltrada.get("page").get("totalElements").asLong()).isZero();
    }

    private String criarOrdem(String placa, StatusOrdemServico status, LocalDateTime dataAbertura) {
        ResponseEntity<String> response = post(
                "/ordens-servico",
                TestUtils.criarOsRequest(TestUtils.CPF_CLIENTE, placa, List.of(servicoId)),
                adminToken);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String numeroOs = parseJson(response.getBody()).get("numeroOs").asText();
        jdbcTemplate.update(
                "UPDATE ordem_servico SET status = ?, data_abertura = ?, ultima_atualizacao = ? WHERE numero_os = ?",
                status.name(),
                dataAbertura,
                dataAbertura,
                numeroOs
        );
        return numeroOs;
    }

    private JsonNode listar(String url) {
        ResponseEntity<String> response = get(url, adminToken);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return parseJson(response.getBody());
    }

    private List<String> numeros(JsonNode content) {
        return content.findValuesAsText("numeroOs");
    }
}
