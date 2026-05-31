package com.autoflow.domain;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemServico.StatusOrdemServico;
import com.autoflow.domain.veiculo.VeiculoEntity;
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
        ClienteEntity cliente = criarCliente(clienteId);
        VeiculoEntity veiculo = criarVeiculo(veiculoId, cliente);
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(veiculo, List.of(servico));

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
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        List<ServicoSolicitadoEntity> servicos = List.of(new ServicoSolicitadoEntity(1L, "Revisao"));

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(null, servicos)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(veiculo, List.of())),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(new VeiculoEntity(), servicos))
        );
    }

    @Test
    void deveProtegerListaDeServicos() {
        List<ServicoSolicitadoEntity> servicos = new ArrayList<>();
        servicos.add(new ServicoSolicitadoEntity(1L, "Revisao"));
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(veiculo, servicos);

        servicos.clear();

        assertEquals(1, ordemServicoEntity.getServicosSolicitados().size());
        assertThrows(UnsupportedOperationException.class, () -> ordemServicoEntity.getServicosSolicitados().clear());
    }

    private ClienteEntity criarCliente(Long clienteId) {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(clienteId);
        cliente.setNome("Cliente " + clienteId);
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("cliente" + clienteId + "@exemplo.com");
        cliente.setTelefone("11999999999");
        return cliente;
    }

    private VeiculoEntity criarVeiculo(Long veiculoId, ClienteEntity cliente) {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(veiculoId);
        veiculo.setCliente(cliente);
        return veiculo;
    }
}
