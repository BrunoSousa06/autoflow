package com.autoflow.domain;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.veiculo.VeiculoEntity;
import org.junit.jupiter.api.Test;

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
        List<ServicoSolicitadoEntity> servicos = List.of(new ServicoSolicitadoEntity(1L, "Revisão"));
        List<ServicoSolicitadoEntity> servicosVazios = List.of();
        VeiculoEntity veiculoSemCliente = new VeiculoEntity();

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(null, servicos)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(veiculo, servicosVazios)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(veiculoSemCliente, servicos))
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

        List<ServicoSolicitadoEntity> servicosSolicitados = ordemServicoEntity.getServicosSolicitados();

        assertEquals(1, servicosSolicitados.size());
        assertThrows(UnsupportedOperationException.class, servicosSolicitados::clear);
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
