package com.autoflow.controller.ordemservico.reparoadicional;

import com.autoflow.controller.ordemservico.request.ItensNecessariosRequest;
import com.autoflow.controller.ordemservico.request.ServicoSolicitadoRequest;
import com.autoflow.controller.ordemservico.reparoadicional.request.CriarReparoAdicionalRequest;
import com.autoflow.controller.ordemservico.reparoadicional.request.ServicoReparoAdicionalRequest;
import com.autoflow.controller.ordemservico.reparoadicional.response.CriarReparoAdicionalResponse;
import com.autoflow.service.ordemservico.reparoadicional.impl.CriarReparoAdicionalResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReparoAdicionalDtoTest {

    @Test
    void deveCriarResponseAPartirDoResult() {
        CriarReparoAdicionalResult result = new CriarReparoAdicionalResult(
                1L,
                2L,
                "http://localhost:8080/public/orcamentos/2?token=abc"
        );

        CriarReparoAdicionalResponse response = CriarReparoAdicionalResponse.from(result);

        assertEquals(1L, response.reparoAdicionalId());
        assertEquals(2L, response.orcamentoId());
        assertEquals("http://localhost:8080/public/orcamentos/2?token=abc", response.publicUrl());
    }

    @Test
    void deveManterValoresDoRequestDeCriacao() {
        ServicoSolicitadoRequest servico = new ServicoSolicitadoRequest(10L);

        CriarReparoAdicionalRequest request = new CriarReparoAdicionalRequest(List.of(servico));

        assertEquals(1, request.servicos().size());
        assertEquals(10L, request.servicos().getFirst().servicoId());
    }

    @Test
    void deveManterValoresDoRequestDeServicoComItens() {
        ItensNecessariosRequest item = new ItensNecessariosRequest(7L, 2);

        ServicoReparoAdicionalRequest request = new ServicoReparoAdicionalRequest(10L, List.of(item));

        assertEquals(10L, request.servicoId());
        assertEquals(1, request.itensNecessarios().size());
        assertEquals(7L, request.itensNecessarios().getFirst().pecaInsumoId());
        assertEquals(2, request.itensNecessarios().getFirst().quantidade());
    }
}
