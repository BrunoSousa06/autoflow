package com.autoflow.application.usecases.orcamento;

import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrcamentoFactoryTest {

    private final OrcamentoFactory factory = new OrcamentoFactory();

    @Test
    void deveGerarOrcamentoPrincipalComTotais() {
        ClienteEntity cliente = criarCliente();
        VeiculoEntity veiculo = criarVeiculo(cliente);
        ServicoSolicitadoEntity servico1 = criarServico(1L, 101L, "S1", new BigDecimal("100.00"));
        servico1.registrarItensNecessarios(List.of(criarItem(5L, "Peca", new BigDecimal("10.00"), 3)));
        ServicoSolicitadoEntity servico2 = criarServico(2L, 102L, "S2", new BigDecimal("50.00"));
        OrdemServicoEntity os = criarOrdemServico(cliente, veiculo, List.of(servico1, servico2));
        LocalDateTime now = LocalDateTime.of(2026, 5, 31, 10, 0);

        OrcamentoEntity orcamento = factory.criarPrincipalDisponivel(os, 1, now);

        assertEquals(TipoOrcamento.PRINCIPAL, orcamento.getTipo());
        assertEquals(StatusOrcamento.DISPONIVEL, orcamento.getStatus());
        assertEquals(new BigDecimal("150.00"), orcamento.getTotalServicos());
        assertEquals(new BigDecimal("30.00"), orcamento.getTotalItens());
        assertEquals(new BigDecimal("180.00"), orcamento.getTotalGeral());
        assertEquals(101L, orcamento.getItens().getFirst().getServicoOsId());
    }

    @Test
    void deveGerarOrcamentoComTotaisZeradosQuandoNaoHaServicos() {
        ClienteEntity cliente = criarCliente();
        OrdemServicoEntity os = criarOrdemServico(cliente, criarVeiculo(cliente), List.of());

        OrcamentoEntity orcamento = factory.criarPrincipalDisponivel(os, 1, LocalDateTime.now());

        assertEquals(BigDecimal.ZERO, orcamento.getTotalServicos());
        assertEquals(BigDecimal.ZERO, orcamento.getTotalItens());
        assertEquals(BigDecimal.ZERO, orcamento.getTotalGeral());
    }

    @Test
    void deveGerarOrcamentoAdicionalComServicosDoReparo() {
        ClienteEntity cliente = criarCliente();
        VeiculoEntity veiculo = criarVeiculo(cliente);
        OrdemServicoEntity os = criarOrdemServico(cliente, veiculo, List.of());
        ServicoSolicitadoEntity adicional = criarServico(2L, 202L, "Servico adicional", new BigDecimal("80.00"));
        adicional.registrarItensNecessarios(List.of(criarItem(7L, "Peca adicional", new BigDecimal("15.00"), 2)));
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(adicional));

        OrcamentoEntity orcamento = factory.criarAdicionalDisponivel(os, reparo, 3, LocalDateTime.now());

        assertEquals(TipoOrcamento.COMPLEMENTAR, orcamento.getTipo());
        assertEquals(new BigDecimal("80.00"), orcamento.getTotalServicos());
        assertEquals(new BigDecimal("30.00"), orcamento.getTotalItens());
        assertEquals(new BigDecimal("110.00"), orcamento.getTotalGeral());
    }

    @Test
    void deveGerarOrcamentoPrincipalConsolidadoComSnapshots() {
        ClienteEntity cliente = criarCliente();
        VeiculoEntity veiculo = criarVeiculo(cliente);
        ServicoSolicitadoEntity principal = criarServico(1L, 101L, "Servico original", new BigDecimal("100.00"));
        principal.registrarItensNecessarios(List.of(criarItem(5L, "Peca original", new BigDecimal("10.00"), 2)));
        OrdemServicoEntity os = criarOrdemServico(cliente, veiculo, List.of(principal));
        ServicoSolicitadoEntity adicional = criarServico(2L, 202L, "Servico adicional", new BigDecimal("80.00"));
        adicional.registrarItensNecessarios(List.of(criarItem(7L, "Peca adicional", new BigDecimal("15.00"), 3)));
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(adicional));

        OrcamentoEntity orcamento = factory.criarPrincipalConsolidadoDisponivel(os, reparo, 4, LocalDateTime.now());

        assertEquals(2, orcamento.getServicos().size());
        assertEquals(2, orcamento.getItens().size());
        assertEquals(new BigDecimal("180.00"), orcamento.getTotalServicos());
        assertEquals(new BigDecimal("65.00"), orcamento.getTotalItens());
        assertEquals(new BigDecimal("245.00"), orcamento.getTotalGeral());
        assertNotNull(orcamento.getCliente());
        assertNotNull(orcamento.getVeiculo());
    }

    private ClienteEntity criarCliente() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("Cliente");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("cliente@exemplo.com");
        cliente.setTelefone("11999999999");
        return cliente;
    }

    private VeiculoEntity criarVeiculo(ClienteEntity cliente) {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(cliente);
        return veiculo;
    }

    private OrdemServicoEntity criarOrdemServico(ClienteEntity cliente, VeiculoEntity veiculo, List<ServicoSolicitadoEntity> servicos) {
        OrdemServicoEntity os = OrdemServicoEntity.criar(cliente, veiculo);
        os.setId(99L);
        os.setNumeroOs("OS-123");
        os.adicionarServicosSolicitados(servicos);
        return os;
    }

    private ServicoSolicitadoEntity criarServico(Long servicoId, Long id, String nome, BigDecimal valor) {
        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(servicoId, nome, valor);
        servico.setId(id);
        return servico;
    }

    private ItemNecessarioEntity criarItem(Long pecaId, String nome, BigDecimal valorUnitario, int quantidade) {
        return ItemNecessarioEntity.criar(pecaId, nome, CategoriaPecaInsumo.PECA, valorUnitario, quantidade, null);
    }
}
