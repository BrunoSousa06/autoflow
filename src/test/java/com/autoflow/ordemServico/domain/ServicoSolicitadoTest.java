package com.autoflow.ordemServico.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServicoSolicitadoTest {

    @Test
    void deveCriarServicoSolicitado() {
        UUID servicoId = UUID.randomUUID();

        ServicoSolicitado servico = new ServicoSolicitado(servicoId, "Alinhamento");

        assertEquals(servicoId, servico.servicoId());
        assertEquals("Alinhamento", servico.nome());
    }

    @Test
    void deveValidarCamposObrigatorios() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitado(null, "Alinhamento")),
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitado(UUID.randomUUID(), null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitado(UUID.randomUUID(), " "))
        );
    }
}
