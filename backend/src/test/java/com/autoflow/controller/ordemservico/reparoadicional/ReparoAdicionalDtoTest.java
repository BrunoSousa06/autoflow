package com.autoflow.controller.ordemservico.reparoadicional;

import com.autoflow.application.dto.ordemservico.reparoadicional.CriarReparoAdicionalOutput;
import com.autoflow.controller.ordemservico.request.ItensNecessariosRequest;
import com.autoflow.controller.ordemservico.reparoadicional.request.CriarReparoAdicionalRequest;
import com.autoflow.controller.ordemservico.reparoadicional.request.ServicoReparoAdicionalRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReparoAdicionalDtoTest {

    private final ReparoAdicionalRestMapper mapper = new ReparoAdicionalRestMapper();

    @Test
    void deveMapearOutputParaResponse() {
        var output = new CriarReparoAdicionalOutput(
                1L,
                2L,
                "http://localhost:8080/public/orcamentos/2?token=abc"
        );

        var response = mapper.toResponse(output);

        assertEquals(1L, response.reparoAdicionalId());
        assertEquals(2L, response.orcamentoId());
        assertEquals("http://localhost:8080/public/orcamentos/2?token=abc", response.publicUrl());
    }

    @Test
    void deveMapearRequestParaCommand() {
        var item = new ItensNecessariosRequest(7L, 2);
        var servico = new ServicoReparoAdicionalRequest(10L, List.of(item));
        var request = new CriarReparoAdicionalRequest(List.of(servico));

        var command = mapper.toCommand("OS-123", "mecanico@autoflow.com", request);

        assertEquals("OS-123", command.numeroOs());
        assertEquals("mecanico@autoflow.com", command.emailMecanico());
        assertEquals(10L, command.servicos().getFirst().servicoId());
        assertEquals(7L, command.servicos().getFirst().itensNecessarios().getFirst().pecaInsumoId());
        assertEquals(2, command.servicos().getFirst().itensNecessarios().getFirst().quantidade());
    }
}
