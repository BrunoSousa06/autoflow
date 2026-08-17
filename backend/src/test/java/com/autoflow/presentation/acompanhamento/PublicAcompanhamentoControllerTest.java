package com.autoflow.presentation.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoPublicoOutput;
import com.autoflow.application.port.in.ordemservico.acompanhamento.ConsultarAcompanhamentoPublicoUseCase;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.presentation.ordemservico.acompanhamento.PublicAcompanhamentoController;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicAcompanhamentoControllerTest {

    @Test
    void deveConsultarEConverterRespostaPublica() {
        var useCase = mock(ConsultarAcompanhamentoPublicoUseCase.class);
        var controller = new PublicAcompanhamentoController(useCase);
        var abertura = LocalDateTime.of(2026, 8, 1, 9, 0);
        var output = new AcompanhamentoPublicoOutput(
                "OS-123", StatusOrdemServico.EM_EXECUCAO, abertura,
                abertura.plusHours(1), null, null, 42L
        );
        when(useCase.execute("token-publico")).thenReturn(output);

        var response = controller.consultar("token-publico");

        assertEquals(output.numeroOs(), response.numeroOs());
        assertEquals(output.status(), response.status());
        assertEquals(output.dataAbertura(), response.dataAbertura());
        assertEquals(output.execucaoIniciadaEm(), response.execucaoIniciadaEm());
        assertEquals(output.finalizadaEm(), response.finalizadaEm());
        assertEquals(output.entregueEm(), response.entregueEm());
        assertEquals(output.orcamentoId(), response.orcamentoId());
    }
}
