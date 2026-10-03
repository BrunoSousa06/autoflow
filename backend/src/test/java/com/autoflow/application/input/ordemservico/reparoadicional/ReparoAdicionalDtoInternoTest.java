package com.autoflow.application.input.ordemservico.reparoadicional;

import com.autoflow.application.output.ordemservico.reparoadicional.CriarReparoAdicionalOutput;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReparoAdicionalDtoInternoTest {

    @Test
    void deveRepresentarComandoDeCriacaoSemTiposDaCamadaRest() {
        ItemReparoAdicionalCommand item = new ItemReparoAdicionalCommand(7L, 2);
        ServicoReparoAdicionalCommand servico = new ServicoReparoAdicionalCommand(10L, List.of(item));

        CriarReparoAdicionalCommand command = new CriarReparoAdicionalCommand(
                "OS-123",
                "mecanico@autoflow.com",
                List.of(servico)
        );

        assertEquals("OS-123", command.numeroOs());
        assertEquals("mecanico@autoflow.com", command.emailMecanico());
        assertEquals(10L, command.servicos().getFirst().servicoId());
        assertEquals(7L, command.servicos().getFirst().itensNecessarios().getFirst().pecaInsumoId());
        assertEquals(2, command.servicos().getFirst().itensNecessarios().getFirst().quantidade());
    }

    @Test
    void deveRepresentarResultadoDaCriacao() {
        CriarReparoAdicionalOutput output = new CriarReparoAdicionalOutput(
                40L,
                30L,
                "http://localhost:8080/public/orcamentos/30?token=abc"
        );

        assertEquals(40L, output.reparoAdicionalId());
        assertEquals(30L, output.orcamentoId());
        assertEquals("http://localhost:8080/public/orcamentos/30?token=abc", output.publicUrl());
    }
}
