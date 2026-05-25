package com.autoflow.domain;

import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemServico.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import java.lang.Long;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoEntityTest {

    @Test
    void deveCriarOrdemServicoComStatusRecebida() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(clienteId, veiculoId, List.of(servico));

        assertNull(ordemServicoEntity.getId());
        assertTrue(ordemServicoEntity.getNumeroOs().startsWith("OS-"));
        assertEquals(clienteId, ordemServicoEntity.getClienteId());
        assertEquals(veiculoId, ordemServicoEntity.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, ordemServicoEntity.getStatus());
        assertNotNull(ordemServicoEntity.getDataAbertura());
        assertEquals(List.of(servico), ordemServicoEntity.getServicosSolicitados());
    }

    @Test
    void deveValidarCamposObrigatorios() {
        List<ServicoSolicitadoEntity> servicos = List.of(new ServicoSolicitadoEntity(1L, "Revisao"));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(null, 1L, servicos)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(1L, null, servicos)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(1L, 1L, List.of()))
        );
    }

    @Test
    void deveProtegerListaDeServicos() {
        List<ServicoSolicitadoEntity> servicos = new ArrayList<>();
        servicos.add(new ServicoSolicitadoEntity(1L, "Revisao"));

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(1L, 1L, servicos);

        servicos.clear();

        assertEquals(1, ordemServicoEntity.getServicosSolicitados().size());
        assertThrows(UnsupportedOperationException.class, () -> ordemServicoEntity.getServicosSolicitados().clear());
    }
}
