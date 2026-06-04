package com.autoflow.service.orcamento;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.ItemNecessarioEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.ServicoSolicitadoEntity;
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

        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo, List.of(servico1, servico2));
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
        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo);
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

        OrdemServicoEntity os = OrdemServicoEntity.criar(veiculo, List.of(servico));
        os.setId(99L);

        OrcamentoEntity orc = factory.criarPrincipalDisponivel(os, 1, LocalDateTime.of(2026, 5, 31, 10, 0));

        assertEquals(BigDecimal.ZERO, orc.getTotalServicos());
        assertEquals(BigDecimal.ZERO, orc.getTotalItens());
        assertEquals(BigDecimal.ZERO, orc.getTotalGeral());
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

