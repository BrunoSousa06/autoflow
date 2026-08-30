package com.autoflow.presentation.ordemservico.response;

import com.autoflow.application.output.ordemservico.StatusOrdemServicoOutput;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StatusOrdemServicoResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void deveMapearOutputPreservandoNumeroStatusEData() {
        LocalDateTime ultimaAtualizacao = LocalDateTime.of(2026, 5, 30, 12, 0);
        StatusOrdemServicoOutput output = new StatusOrdemServicoOutput(
                "OS-123", StatusOrdemServico.ENTREGUE, ultimaAtualizacao);

        StatusOrdemServicoResponse response = StatusOrdemServicoResponse.from(output);

        assertEquals("OS-123", response.numeroOs());
        assertEquals(StatusOrdemServico.ENTREGUE, response.status());
        assertEquals(ultimaAtualizacao, response.ultimaAtualizacao());
    }

    @Test
    void deveSerializarSomenteOsCamposDoContratoEnxuto() throws Exception {
        StatusOrdemServicoResponse response = new StatusOrdemServicoResponse(
                "OS-123",
                StatusOrdemServico.EM_EXECUCAO,
                LocalDateTime.of(2026, 5, 30, 12, 0));

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        Set<String> fields = new HashSet<>();
        json.fieldNames().forEachRemaining(fields::add);
        assertEquals(Set.of("numeroOs", "status", "ultimaAtualizacao"), fields);
        assertEquals(3, json.size());
        assertEquals("OS-123", json.get("numeroOs").asText());
        assertEquals("EM_EXECUCAO", json.get("status").asText());
        assertEquals("2026-05-30T12:00:00", json.get("ultimaAtualizacao").asText());
        assertFalse(json.has("id"));
        assertFalse(json.has("cliente"));
        assertFalse(json.has("veiculo"));
        assertFalse(json.has("servicos"));
        assertFalse(json.has("orcamentoAtual"));
        assertFalse(json.has("diagnostico"));
    }
}
