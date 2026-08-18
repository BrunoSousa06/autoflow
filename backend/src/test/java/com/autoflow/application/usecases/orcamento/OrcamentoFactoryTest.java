package com.autoflow.application.usecases.orcamento;

import com.autoflow.domain.cliente.Cliente;
import com.autoflow.domain.orcamento.Orcamento;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.ItemNecessario;
import com.autoflow.domain.ordemservico.OrdemServico;
import com.autoflow.domain.ordemservico.ServicoSolicitado;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicional;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.veiculo.Veiculo;
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
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo(cliente);
        ServicoSolicitado servico1 = criarServico(1L, 101L, "S1", new BigDecimal("100.00"));
        servico1.registrarItensNecessarios(List.of(criarItem(5L, "Peca", new BigDecimal("10.00"), 3)));
        ServicoSolicitado servico2 = criarServico(2L, 102L, "S2", new BigDecimal("50.00"));
        OrdemServico os = criarOrdemServico(cliente, veiculo, List.of(servico1, servico2));
        LocalDateTime now = LocalDateTime.of(2026, 5, 31, 10, 0);

        Orcamento orcamento = factory.criarPrincipalDisponivel(os, 1, now);

        assertEquals(TipoOrcamento.PRINCIPAL, orcamento.getTipo());
        assertEquals(StatusOrcamento.DISPONIVEL, orcamento.getStatus());
        assertEquals(new BigDecimal("150.00"), orcamento.getTotalServicos());
        assertEquals(new BigDecimal("30.00"), orcamento.getTotalItens());
        assertEquals(new BigDecimal("180.00"), orcamento.getTotalGeral());
        assertEquals(101L, orcamento.getItens().getFirst().getServicoOsId());
    }

    @Test
    void deveGerarOrcamentoComTotaisZeradosQuandoNaoHaServicos() {
        Cliente cliente = criarCliente();
        OrdemServico os = criarOrdemServico(cliente, criarVeiculo(cliente), List.of());

        Orcamento orcamento = factory.criarPrincipalDisponivel(os, 1, LocalDateTime.now());

        assertEquals(BigDecimal.ZERO, orcamento.getTotalServicos());
        assertEquals(BigDecimal.ZERO, orcamento.getTotalItens());
        assertEquals(BigDecimal.ZERO, orcamento.getTotalGeral());
    }

    @Test
    void deveGerarOrcamentoAdicionalComServicosDoReparo() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo(cliente);
        OrdemServico os = criarOrdemServico(cliente, veiculo, List.of());
        ServicoSolicitado adicional = criarServico(2L, 202L, "Servico adicional", new BigDecimal("80.00"));
        adicional.registrarItensNecessarios(List.of(criarItem(7L, "Peca adicional", new BigDecimal("15.00"), 2)));
        ReparoAdicional reparo = ReparoAdicional.criar("OS-123", 20L, List.of(adicional));

        Orcamento orcamento = factory.criarAdicionalDisponivel(os, reparo, 3, LocalDateTime.now());

        assertEquals(TipoOrcamento.COMPLEMENTAR, orcamento.getTipo());
        assertEquals(new BigDecimal("80.00"), orcamento.getTotalServicos());
        assertEquals(new BigDecimal("30.00"), orcamento.getTotalItens());
        assertEquals(new BigDecimal("110.00"), orcamento.getTotalGeral());
    }

    @Test
    void deveGerarOrcamentoPrincipalConsolidadoComSnapshots() {
        Cliente cliente = criarCliente();
        Veiculo veiculo = criarVeiculo(cliente);
        ServicoSolicitado principal = criarServico(1L, 101L, "Servico original", new BigDecimal("100.00"));
        principal.registrarItensNecessarios(List.of(criarItem(5L, "Peca original", new BigDecimal("10.00"), 2)));
        OrdemServico os = criarOrdemServico(cliente, veiculo, List.of(principal));
        ServicoSolicitado adicional = criarServico(2L, 202L, "Servico adicional", new BigDecimal("80.00"));
        adicional.registrarItensNecessarios(List.of(criarItem(7L, "Peca adicional", new BigDecimal("15.00"), 3)));
        ReparoAdicional reparo = ReparoAdicional.criar("OS-123", 20L, List.of(adicional));

        Orcamento orcamento = factory.criarPrincipalConsolidadoDisponivel(os, reparo, 4, LocalDateTime.now());

        assertEquals(2, orcamento.getServicos().size());
        assertEquals(2, orcamento.getItens().size());
        assertEquals(new BigDecimal("180.00"), orcamento.getTotalServicos());
        assertEquals(new BigDecimal("65.00"), orcamento.getTotalItens());
        assertEquals(new BigDecimal("245.00"), orcamento.getTotalGeral());
        assertNotNull(orcamento.getCliente());
        assertNotNull(orcamento.getVeiculo());
    }

    private Cliente criarCliente() {
        return Cliente.reconstituir(1L, "Cliente", "12345678901", "11999999999", "cliente@exemplo.com");
    }

    private Veiculo criarVeiculo(Cliente cliente) {
        return new Veiculo(10L, "ABC1D23", "Honda", "Civic", 2020);
    }

    private OrdemServico criarOrdemServico(Cliente cliente, Veiculo veiculo, List<ServicoSolicitado> servicos) {
        OrdemServico os = OrdemServico.criar(cliente, veiculo);
        os.setId(99L);
        os.setNumeroOs("OS-123");
        os.adicionarServicosSolicitados(servicos);
        return os;
    }

    private ServicoSolicitado criarServico(Long servicoId, Long id, String nome, BigDecimal valor) {
        ServicoSolicitado servico = new ServicoSolicitado(servicoId, nome, valor);
        servico.setId(id);
        return servico;
    }

    private ItemNecessario criarItem(Long pecaId, String nome, BigDecimal valorUnitario, int quantidade) {
        return ItemNecessario.criar(pecaId, nome, CategoriaPecaInsumo.PECA, valorUnitario, quantidade, null);
    }
}
