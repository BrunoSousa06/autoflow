package com.autoflow.controller.ordemservico.acompanhamento.response;

import com.autoflow.domain.ordemservico.StatusServicoOs;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ServicoAcompanhamentoResponseTest {

    @Test
    void deveCriarResponseComTodosOsCampos() {
        ServicoAcompanhamentoResponse response = new ServicoAcompanhamentoResponse(
                1L, "Troca de óleo", BigDecimal.valueOf(150.00), StatusServicoOs.EM_EXECUCAO
        );

        assertEquals(1L, response.id());
        assertEquals("Troca de óleo", response.nome());
        assertEquals(BigDecimal.valueOf(150.00), response.valor());
        assertEquals(StatusServicoOs.EM_EXECUCAO, response.status());
    }

    @Test
    void deveCriarResponseComCamposNulos() {
        ServicoAcompanhamentoResponse response = new ServicoAcompanhamentoResponse(null, null, null, null);

        assertNull(response.id());
        assertNull(response.nome());
        assertNull(response.valor());
        assertNull(response.status());
    }

    @Test
    void deveSerIgualQuandoMesmosValores() {
        ServicoAcompanhamentoResponse r1 = new ServicoAcompanhamentoResponse(1L, "Serviço", BigDecimal.TEN, StatusServicoOs.AGUARDANDO);
        ServicoAcompanhamentoResponse r2 = new ServicoAcompanhamentoResponse(1L, "Serviço", BigDecimal.TEN, StatusServicoOs.AGUARDANDO);
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void deveSerDiferenteQuandoStatusDiferente() {
        ServicoAcompanhamentoResponse r1 = new ServicoAcompanhamentoResponse(1L, "Serviço", BigDecimal.TEN, StatusServicoOs.AGUARDANDO);
        ServicoAcompanhamentoResponse r2 = new ServicoAcompanhamentoResponse(1L, "Serviço", BigDecimal.TEN, StatusServicoOs.FINALIZADO);
        assertNotEquals(r1, r2);
    }

    @Test
    void deveGerarToStringComInformacoes() {
        ServicoAcompanhamentoResponse response = new ServicoAcompanhamentoResponse(1L, "Troca de óleo", BigDecimal.valueOf(150), StatusServicoOs.EM_EXECUCAO);
        String str = response.toString();
        assertTrue(str.contains("Troca de óleo"));
        assertTrue(str.contains("EM_EXECUCAO"));
    }
}
