package com.autoflow.ordemServico.domain;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {

    @Test
    void deveCriarOrdemServicoComStatusRecebida() {
        UUID clienteId = UUID.randomUUID();
        UUID veiculoId = UUID.randomUUID();
        ServicoSolicitado servico = new ServicoSolicitado(UUID.randomUUID(), "Revisao");

        OrdemServico ordemServico = OrdemServico.criar(clienteId, veiculoId, List.of(servico));

        assertNotNull(ordemServico.getId());
        assertTrue(ordemServico.getNumeroOs().startsWith("OS-"));
        assertEquals(clienteId, ordemServico.getClienteId());
        assertEquals(veiculoId, ordemServico.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, ordemServico.getStatus());
        assertNotNull(ordemServico.getDataAbertura());
        assertEquals(List.of(servico), ordemServico.getServicosSolicitados());
    }

    @Test
    void deveValidarCamposObrigatorios() {
        List<ServicoSolicitado> servicos = List.of(new ServicoSolicitado(UUID.randomUUID(), "Revisao"));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServico.criar(null, UUID.randomUUID(), servicos)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServico.criar(UUID.randomUUID(), null, servicos)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServico.criar(UUID.randomUUID(), UUID.randomUUID(), List.of()))
        );
    }

    @Test
    void deveProtegerListaDeServicos() {
        List<ServicoSolicitado> servicos = new ArrayList<>();
        servicos.add(new ServicoSolicitado(UUID.randomUUID(), "Revisao"));

        OrdemServico ordemServico = OrdemServico.criar(UUID.randomUUID(), UUID.randomUUID(), servicos);

        servicos.clear();

        assertEquals(1, ordemServico.getServicosSolicitados().size());
        assertThrows(UnsupportedOperationException.class, () -> ordemServico.getServicosSolicitados().clear());
    }
}
