package com.autoflow.presentation.ordemservico.request;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IncluirOrdemServicoRequestTest {

    @Test
    void deveCriarRequestComListaDeServicos() {
        ServicoSolicitadoRequest servico = new ServicoSolicitadoRequest(1L);
        IncluirOrdemServicoRequest request = new IncluirOrdemServicoRequest(List.of(servico));

        assertNotNull(request.servicosSolicitados());
        assertEquals(1, request.servicosSolicitados().size());
        assertEquals(1L, request.servicosSolicitados().getFirst().servicoId());
    }

    @Test
    void deveCriarRequestComListaVazia() {
        IncluirOrdemServicoRequest request = new IncluirOrdemServicoRequest(List.of());
        assertNotNull(request.servicosSolicitados());
        assertTrue(request.servicosSolicitados().isEmpty());
    }

    @Test
    void deveCriarRequestComListaNula() {
        IncluirOrdemServicoRequest request = new IncluirOrdemServicoRequest(null);
        assertNull(request.servicosSolicitados());
    }

    @Test
    void deveSerIgualQuandoMesmosValores() {
        ServicoSolicitadoRequest servico = new ServicoSolicitadoRequest(1L);
        IncluirOrdemServicoRequest r1 = new IncluirOrdemServicoRequest(List.of(servico));
        IncluirOrdemServicoRequest r2 = new IncluirOrdemServicoRequest(List.of(servico));
        assertEquals(r1, r2);
    }
}
