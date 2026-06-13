package com.autoflow.service.orcamento;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.veiculo.VeiculoEntity;
import com.autoflow.service.orcamento.impl.OrcamentoFactoryImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrcamentoFactoryImplTest {

    private final OrcamentoFactoryImpl factory = new OrcamentoFactoryImpl();

    @Test
    void deveGerarOrcamentoPrincipalComTotais() {
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("Cliente");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("cliente@exemplo.com");
        cliente.setTelefone("11999999999");

        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(cliente);

        ServicoSolicitadoEntity servico1 = new ServicoSolicitadoEntity(1L, "S1", new BigDecimal("100.00"));
        servico1.setId(101L);
        servico1.registrarItensNecessarios(List.of(
                ItemNecessarioEntity.criar(
                        5L,
                        "Peca",
                        CategoriaPecaInsumo.PECA,
                        new BigDecimal("10.00"),
                        3,
                        null
                )
        ));

        ServicoSolicitadoEntity servico2 = new ServicoSolicitadoEntity(2L, "S2", new BigDecimal("50.00"));
        servico2.setId(102L);

        OrdemServicoEntity os = OrdemServicoEntity.criar(cliente, veiculo);
        os.adicionarServicos(List.of(servico1, servico2));
        os.setId(99L);

        LocalDateTime now = LocalDateTime.of(2026, 5, 31, 10, 0);

        OrcamentoEntity orc = factory.criarPrincipalDisponivel(os, 1, now);

        assertEquals(TipoOrcamento.PRINCIPAL, orc.getTipo());
        assertEquals(StatusOrcamento.DISPONIVEL, orc.getStatus());
        assertEquals(1, orc.getVersao());
        assertEquals(now, orc.getCriadoEm());
        assertNotNull(orc.getServicos());
        assertNotNull(orc.getItens());

        assertEquals(new BigDecimal("150.00"), orc.getTotalServicos());
        assertEquals(new BigDecimal("30.00"), orc.getTotalItens());
        assertEquals(new BigDecimal("180.00"), orc.getTotalGeral());
        assertEquals(101L, orc.getItens().getFirst().getServicoOsId());
    }

    @Test
    void deveGerarOrcamentoComTotaisZeradosQuandoOsNaoTemServicos() {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(criarCliente());
        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo.getCliente(), veiculo);
        os.setId(99L);

        OrcamentoEntity orc = factory.criarPrincipalDisponivel(os, 1, LocalDateTime.of(2026, 5, 31, 10, 0));

        assertEquals(BigDecimal.ZERO, orc.getTotalServicos());
        assertEquals(BigDecimal.ZERO, orc.getTotalItens());
        assertEquals(BigDecimal.ZERO, orc.getTotalGeral());
    }

    @Test
    void deveIgnorarValoresNulosAoCalcularTotais() {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(criarCliente());

        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity();
        servico.setId(101L);
        servico.setServicoId(1L);
        servico.setNome("S1");
        servico.setValor(null);

        ItemNecessarioEntity item = new ItemNecessarioEntity();
        item.setPecaInsumoId(5L);
        item.setNome("Peca");
        item.setTipo(CategoriaPecaInsumo.PECA);
        item.setValorUnitario(new BigDecimal("10.00"));
        item.setQuantidade(1);
        item.setValorTotal(null);
        servico.registrarItensNecessarios(List.of(item));

        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo.getCliente(), veiculo);
        os.adicionarServicos(List.of(servico));
        os.setId(99L);

        OrcamentoEntity orc = factory.criarPrincipalDisponivel(os, 1, LocalDateTime.of(2026, 5, 31, 10, 0));

        assertEquals(BigDecimal.ZERO, orc.getTotalServicos());
        assertEquals(BigDecimal.ZERO, orc.getTotalItens());
        assertEquals(BigDecimal.ZERO, orc.getTotalGeral());
    }

    @Test
    void deveGerarOrcamentoAdicionalComServicosDoReparo() {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(criarCliente());

        ServicoSolicitadoEntity servicoOriginal = new ServicoSolicitadoEntity(1L, "Servico original", new BigDecimal("100.00"));
        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo.getCliente(), veiculo);
        os.adicionarServicos(List.of(servicoOriginal));
        os.setId(99L);

        ServicoSolicitadoEntity servicoAdicional = new ServicoSolicitadoEntity(2L, "Servico adicional", new BigDecimal("80.00"));
        servicoAdicional.setId(202L);
        servicoAdicional.registrarItensNecessarios(List.of(
                ItemNecessarioEntity.criar(
                        7L,
                        "Peca adicional",
                        CategoriaPecaInsumo.PECA,
                        new BigDecimal("15.00"),
                        2,
                        null
                )
        ));
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(servicoAdicional));
        reparo.setId(55L);

        LocalDateTime now = LocalDateTime.of(2026, 6, 4, 10, 0);

        OrcamentoEntity orc = factory.criarAdicionalDisponivel(os, reparo, 3, now);

        assertEquals(99L, orc.getOrdemServicoId());
        assertEquals(TipoOrcamento.ADICIONAL, orc.getTipo());
        assertEquals(StatusOrcamento.DISPONIVEL, orc.getStatus());
        assertEquals(3, orc.getVersao());
        assertEquals(now, orc.getCriadoEm());
        assertEquals(now, orc.getDisponibilizadoEm());

        assertEquals(1, orc.getServicos().size());
        assertEquals(2L, orc.getServicos().getFirst().getServicoId());
        assertEquals("Servico adicional", orc.getServicos().getFirst().getNome());

        assertEquals(1, orc.getItens().size());
        assertEquals(202L, orc.getItens().getFirst().getServicoOsId());
        assertEquals(7L, orc.getItens().getFirst().getPecaInsumoId());

        assertEquals(new BigDecimal("80.00"), orc.getTotalServicos());
        assertEquals(new BigDecimal("30.00"), orc.getTotalItens());
        assertEquals(new BigDecimal("110.00"), orc.getTotalGeral());
    }

    @Test
    void deveGerarOrcamentoAdicionalComTotaisZeradosQuandoReparoNaoTemServicos() {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(criarCliente());
        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo.getCliente(), veiculo);
        os.setId(99L);

        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of());

        OrcamentoEntity orc = factory.criarAdicionalDisponivel(os, reparo, 1, LocalDateTime.of(2026, 6, 4, 10, 0));

        assertEquals(TipoOrcamento.ADICIONAL, orc.getTipo());
        assertEquals(BigDecimal.ZERO, orc.getTotalServicos());
        assertEquals(BigDecimal.ZERO, orc.getTotalItens());
        assertEquals(BigDecimal.ZERO, orc.getTotalGeral());
    }

    @Test
    void deveGerarOrcamentoPrincipalConsolidadoComServicosDaOsEReparo() {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(criarCliente());

        ServicoSolicitadoEntity servicoOriginal = new ServicoSolicitadoEntity(1L, "Servico original", new BigDecimal("100.00"));
        servicoOriginal.setId(101L);
        servicoOriginal.registrarItensNecessarios(List.of(
                ItemNecessarioEntity.criar(
                        5L,
                        "Peca original",
                        CategoriaPecaInsumo.PECA,
                        new BigDecimal("10.00"),
                        2,
                        null
                )
        ));

        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo.getCliente(), veiculo);
        os.adicionarServicos(List.of(servicoOriginal));
        os.setId(99L);

        ServicoSolicitadoEntity servicoAdicional = new ServicoSolicitadoEntity(2L, "Servico adicional", new BigDecimal("80.00"));
        servicoAdicional.setId(202L);
        servicoAdicional.registrarItensNecessarios(List.of(
                ItemNecessarioEntity.criar(
                        7L,
                        "Peca adicional",
                        CategoriaPecaInsumo.PECA,
                        new BigDecimal("15.00"),
                        3,
                        null
                )
        ));
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of(servicoAdicional));
        LocalDateTime now = LocalDateTime.of(2026, 6, 4, 11, 0);

        OrcamentoEntity orc = factory.criarPrincipalConsolidadoDisponivel(os, reparo, 4, now);

        assertEquals(99L, orc.getOrdemServicoId());
        assertEquals(TipoOrcamento.PRINCIPAL, orc.getTipo());
        assertEquals(StatusOrcamento.DISPONIVEL, orc.getStatus());
        assertEquals(4, orc.getVersao());
        assertEquals(now, orc.getCriadoEm());
        assertEquals(now, orc.getDisponibilizadoEm());
        assertEquals(2, orc.getServicos().size());
        assertEquals(2, orc.getItens().size());
        assertEquals(101L, orc.getItens().getFirst().getServicoOsId());
        assertEquals(202L, orc.getItens().getLast().getServicoOsId());
        assertEquals(new BigDecimal("180.00"), orc.getTotalServicos());
        assertEquals(new BigDecimal("65.00"), orc.getTotalItens());
        assertEquals(new BigDecimal("245.00"), orc.getTotalGeral());
        assertNotNull(orc.getCliente());
        assertNotNull(orc.getVeiculo());
    }

    @Test
    void deveGerarOrcamentoPrincipalConsolidadoComTotaisZeradosQuandoOsEReparoNaoTemServicos() {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(criarCliente());

        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo.getCliente(), veiculo);
        os.setId(99L);
        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of());

        OrcamentoEntity orc = factory.criarPrincipalConsolidadoDisponivel(
                os,
                reparo,
                2,
                LocalDateTime.of(2026, 6, 4, 11, 0)
        );

        assertEquals(TipoOrcamento.PRINCIPAL, orc.getTipo());
        assertEquals(StatusOrcamento.DISPONIVEL, orc.getStatus());
        assertEquals(0, orc.getServicos().size());
        assertEquals(0, orc.getItens().size());
        assertEquals(BigDecimal.ZERO, orc.getTotalServicos());
        assertEquals(BigDecimal.ZERO, orc.getTotalItens());
        assertEquals(BigDecimal.ZERO, orc.getTotalGeral());
        assertNotNull(orc.getCliente());
        assertNotNull(orc.getVeiculo());
    }

    @Test
    void deveManterSnapshotDosItensMesmoQuandoOrigemMudaAposConsolidacao() {
        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(criarCliente());

        ItemNecessarioEntity item = ItemNecessarioEntity.criar(
                5L,
                "Peca original",
                CategoriaPecaInsumo.PECA,
                new BigDecimal("10.00"),
                2,
                null
        );
        ServicoSolicitadoEntity servicoOriginal = new ServicoSolicitadoEntity(1L, "Servico original", new BigDecimal("100.00"));
        servicoOriginal.setId(101L);
        servicoOriginal.registrarItensNecessarios(List.of(item));

        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo.getCliente(), veiculo);
        os.adicionarServicos(List.of(servicoOriginal));
        os.setId(99L);

        ReparoAdicionalEntity reparo = ReparoAdicionalEntity.criar("OS-123", 20L, List.of());
        OrcamentoEntity orc = factory.criarPrincipalConsolidadoDisponivel(
                os,
                reparo,
                2,
                LocalDateTime.of(2026, 6, 4, 11, 0)
        );

        item.setNome("Peca indisponivel");
        item.setQuantidade(0);
        item.setValorUnitario(new BigDecimal("99.00"));
        item.setValorTotal(BigDecimal.ZERO);

        assertEquals(1, orc.getItens().size());
        assertEquals("Peca original", orc.getItens().getFirst().getNome());
        assertEquals(2, orc.getItens().getFirst().getQuantidade());
        assertEquals(new BigDecimal("10.00"), orc.getItens().getFirst().getValorUnitario());
        assertEquals(new BigDecimal("20.00"), orc.getItens().getFirst().getValorTotal());
        assertEquals(new BigDecimal("20.00"), orc.getTotalItens());
        assertEquals(new BigDecimal("120.00"), orc.getTotalGeral());
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
}
