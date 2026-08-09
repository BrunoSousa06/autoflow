package com.autoflow.integration;

import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.integration.config.AbstractIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Acompanhamento público - Testes de Integração")
class PublicAcompanhamentoIT extends AbstractIT {

    private static final String TOKEN = "token-publico-de-integracao";

    @Autowired
    private TokenAcompanhamentoGateway tokenGateway;

    @BeforeEach
    void configurar() {
        limparBancoDeDados();
    }

    @Test
    @DisplayName("deve consultar acompanhamento sem autenticação")
    void deveConsultarAcompanhamentoSemAutenticacao() {
        Long ordemServicoId = inserirOrdemServico(TOKEN, LocalDateTime.now().plusDays(1));
        Long orcamentoId = jdbcTemplate.queryForObject("""
                INSERT INTO orcamento (
                    ordem_servico_id, numero_os, tipo, versao, status, criado_em,
                    disponibilizado_em, total_servicos, total_itens, total_geral
                ) VALUES (?, 'OS-PUBLICA-1', 'PRINCIPAL', 1, 'DISPONIVEL', NOW(), NOW(), 100, 20, 120)
                RETURNING id
                """, Long.class, ordemServicoId);

        var response = restTemplate.getForEntity(
                "/public/ordens-servico/acompanhamento?token=" + TOKEN,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        var json = parseJson(response.getBody());
        assertThat(json.get("numeroOs").asText()).isEqualTo("OS-PUBLICA-1");
        assertThat(json.get("status").asText()).isEqualTo("EM_EXECUCAO");
        assertThat(json.get("orcamentoId").asLong()).isEqualTo(orcamentoId);
        assertThat(json.get("dataAbertura").isTextual()).isTrue();
        assertThat(json.has("email")).isFalse();
        assertThat(json.has("cpfCnpj")).isFalse();
        assertThat(json.has("telefone")).isFalse();
    }

    @Test
    @DisplayName("deve baixar PDF e aprovar orçamento com token de acompanhamento")
    void deveBaixarPdfEAprovarOrcamentoComTokenDeAcompanhamento() {
        Long ordemServicoId = inserirOrdemServicoParaAprovacao(
                TOKEN, LocalDateTime.now().plusDays(1));
        Long orcamentoId = inserirOrcamentoDisponivel(ordemServicoId);

        var pdfResponse = restTemplate.getForEntity(
                "/public/orcamentos/" + orcamentoId + "/pdf/acompanhamento?token=" + TOKEN,
                byte[].class
        );

        assertThat(pdfResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(pdfResponse.getHeaders().getContentType())
                .isEqualTo(org.springframework.http.MediaType.APPLICATION_PDF);
        assertThat(pdfResponse.getHeaders().getFirst("Content-Disposition"))
                .isEqualTo("attachment; filename=\"orcamento-" + orcamentoId + ".pdf\"");
        assertThat(pdfResponse.getBody()).isNotEmpty();
        assertThat(new String(pdfResponse.getBody(), 0, 4)).isEqualTo("%PDF");

        var approvalResponse = restTemplate.postForEntity(
                "/public/orcamentos/" + orcamentoId + "/aprovar/acompanhamento?token=" + TOKEN,
                null,
                String.class
        );

        assertThat(approvalResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(parseJson(approvalResponse.getBody()).get("status").asText())
                .isEqualTo("APROVADO");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM orcamento WHERE id = ?", String.class, orcamentoId))
                .isEqualTo("APROVADO");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM ordem_servico WHERE id = ?", String.class, ordemServicoId))
                .isEqualTo("EM_EXECUCAO");
    }

    @Test
    @DisplayName("deve retornar 400 quando token estiver vazio")
    void deveRetornar400QuandoTokenEstiverVazio() {
        var response = restTemplate.getForEntity(
                "/public/ordens-servico/acompanhamento?token=",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Token de acompanhamento");
    }

    @Test
    @DisplayName("deve retornar 404 quando token for inválido")
    void deveRetornar404QuandoTokenForInvalido() {
        var response = restTemplate.getForEntity(
                "/public/ordens-servico/acompanhamento?token=invalido",
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Acompanhamento público não encontrado");
    }

    @Test
    @DisplayName("deve retornar 404 quando token estiver expirado")
    void deveRetornar404QuandoTokenEstiverExpirado() {
        inserirOrdemServico(TOKEN, LocalDateTime.now().minusMinutes(1));

        var response = restTemplate.getForEntity(
                "/public/ordens-servico/acompanhamento?token=" + TOKEN,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("deve retornar 404 quando token estiver revogado")
    void deveRetornar404QuandoTokenEstiverRevogado() {
        Long ordemServicoId = inserirOrdemServico(TOKEN, LocalDateTime.now().plusDays(1));
        jdbcTemplate.update(
                "UPDATE ordem_servico SET acompanhamento_token_revogado_em = NOW() WHERE id = ?",
                ordemServicoId
        );

        var response = restTemplate.getForEntity(
                "/public/ordens-servico/acompanhamento?token=" + TOKEN,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Long inserirOrdemServicoParaAprovacao(String token, LocalDateTime expiraEm) {
        return inserirOrdemServico(
                token,
                expiraEm,
                "AGUARDANDO_APROVACAO",
                null
        );
    }

    private Long inserirOrcamentoDisponivel(Long ordemServicoId) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO orcamento (
                    ordem_servico_id, numero_os, tipo, versao, status, criado_em,
                    disponibilizado_em, total_servicos, total_itens, total_geral,
                    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
                    veiculo_placa, veiculo_marca, veiculo_modelo, veiculo_ano
                ) VALUES (
                    ?, 'OS-PUBLICA-1', 'PRINCIPAL', 1, 'DISPONIVEL', NOW(), NOW(),
                    100, 20, 120, 'Cliente IT', '12345678901',
                    'cliente.it@autoflow.com', '11999999999', 'ABC1D23',
                    'Honda', 'Civic', 2020
                ) RETURNING id
                """, Long.class, ordemServicoId);
    }

    private Long inserirOrdemServico(String token, LocalDateTime expiraEm) {
        return inserirOrdemServico(
                token,
                expiraEm,
                "EM_EXECUCAO",
                LocalDateTime.now().minusHours(2)
        );
    }

    private Long inserirOrdemServico(
            String token,
            LocalDateTime expiraEm,
            String status,
            LocalDateTime execucaoIniciadaEm
    ) {
        Long clienteId = jdbcTemplate.queryForObject("""
                INSERT INTO clientes (nome, cpf_cnpj, telefone, email)
                VALUES ('Cliente IT', '12345678901', '11999999999', 'cliente.it@autoflow.com')
                RETURNING id
                """, Long.class);
        Long veiculoId = jdbcTemplate.queryForObject("""
                INSERT INTO veiculos (marca, modelo, ano, placa, cliente_id)
                VALUES ('Honda', 'Civic', 2020, 'ABC1D23', ?)
                RETURNING id
                """, Long.class, clienteId);

        LocalDateTime criadoEm = LocalDateTime.now().minusHours(1);
        return jdbcTemplate.queryForObject("""
                INSERT INTO ordem_servico (
                    numero_os, cliente_id, veiculo_id, status, data_abertura,
                    cliente_nome, cliente_cpf_cnpj, cliente_email, cliente_telefone,
                    execucao_iniciada_em, ultima_atualizacao,
                    acompanhamento_token_hash, acompanhamento_token_criado_em,
                    acompanhamento_token_expira_em
                ) VALUES (
                    'OS-PUBLICA-1', ?, ?, ?, NOW() - INTERVAL '1 day',
                    'Cliente IT', '12345678901', 'cliente.it@autoflow.com', '11999999999',
                    ?, NOW(), ?, ?, ?
                ) RETURNING id
                """, Long.class, clienteId, veiculoId, status, execucaoIniciadaEm,
                tokenGateway.calcularHash(token), criadoEm, expiraEm);
    }
}
