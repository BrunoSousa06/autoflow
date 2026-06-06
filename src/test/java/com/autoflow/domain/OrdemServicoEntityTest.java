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
    void deveCriarOrdemServicoComStatusRecebidaESnapshotDoCliente() {
        Long clienteId = 1L;
        Long veiculoId = 1L;
        ClienteEntity cliente = criarCliente(clienteId);
        VeiculoEntity veiculo = criarVeiculo(veiculoId, cliente);
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(1L, "Revisao");

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(cliente, veiculo);
        ordemServicoEntity.adicionarServicos(List.of(servico));

        assertNull(ordemServicoEntity.getId());
        assertTrue(ordemServicoEntity.getNumeroOs().startsWith("OS-"));
        assertEquals(clienteId, ordemServicoEntity.getClienteId());
        assertEquals(cliente.getCpfCnpj(), ordemServicoEntity.getCliente().getCpfCnpj());
        assertEquals(cliente.getEmail(), ordemServicoEntity.getCliente().getEmail());
        assertEquals(veiculoId, ordemServicoEntity.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, ordemServicoEntity.getStatus());
        assertNotNull(ordemServicoEntity.getDataAbertura());
        assertEquals(List.of(servico), ordemServicoEntity.getServicosSolicitados());
    }

    @Test
    void deveValidarCamposObrigatorios() {
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        List<ServicoSolicitadoEntity> servicosVazios = List.of();

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(cliente, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(null, veiculo)),
                () -> {
                    OrdemServicoEntity os = OrdemServicoEntity.criar(cliente, veiculo);
                    assertThrows(IllegalArgumentException.class, () -> os.adicionarServicos(servicosVazios));
                }
        );
    }

    @Test
    void deveProtegerListaDeServicos() {
        List<ServicoSolicitadoEntity> servicos = new ArrayList<>();
        servicos.add(new ServicoSolicitadoEntity(1L, "Revisao"));
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);

        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(cliente, veiculo);
        ordemServicoEntity.adicionarServicos(servicos);

        servicos.clear();

        List<ServicoSolicitadoEntity> servicosSolicitados = ordemServicoEntity.getServicosSolicitados();

        assertEquals(1, servicosSolicitados.size());
        assertThrows(UnsupportedOperationException.class, servicosSolicitados::clear);
    }

    @Test
    void deveEntregarOrdemServico() {
        ClienteEntity cliente = criarCliente(1L);
        VeiculoEntity veiculo = criarVeiculo(1L, cliente);
        OrdemServicoEntity ordemServicoEntity = OrdemServicoEntity.criar(cliente, veiculo);
        ordemServicoEntity.adicionarServicos(List.of(new ServicoSolicitadoEntity(1L, "Revisao")));

        ordemServicoEntity.entregar();

        assertEquals(StatusOrdemServico.ENTREGUE, ordemServicoEntity.getStatus());
        assertNotNull(ordemServicoEntity.getEntregueEm());
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
