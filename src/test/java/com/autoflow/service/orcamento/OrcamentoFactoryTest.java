package com.autoflow.service.orcamento;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemServico.ItemNecessarioEntity;
import com.autoflow.domain.ordemServico.OrdemServicoEntity;
import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import com.autoflow.domain.pecaInsumo.CategoriaPecaInsumo;
import com.autoflow.domain.veiculo.VeiculoEntity;
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
        ClienteEntity cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("Cliente");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("cliente@exemplo.com");
        cliente.setTelefone("11999999999");

        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(10L);
        veiculo.setCliente(cliente);

        OrdemServicoEntity os = OrdemServicoEntity.criar(
                veiculo,
                List.of(
                        new ServicoSolicitadoEntity(1L, "S1", new BigDecimal("100.00")),
                        new ServicoSolicitadoEntity(2L, "S2", new BigDecimal("50.00"))
                )
        );
        os.setId(99L);

        os.adicionarItensNecessarios(List.of(
                ItemNecessarioEntity.criar(
                        5L,
                        "Peca",
                        CategoriaPecaInsumo.PECA,
                        new BigDecimal("10.00"),
                        3,
                        null
                )
        ));

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
    }
}

