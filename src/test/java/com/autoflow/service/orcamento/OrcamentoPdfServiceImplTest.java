package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.ClienteOrcamentoSnapshot;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.OrcamentoItemNecessarioEntity;
import com.autoflow.domain.orcamento.OrcamentoServicoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.orcamento.VeiculoOrcamentoSnapshot;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.service.orcamento.impl.OrcamentoPdfServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
class OrcamentoPdfServiceImplTest {

    private final OrcamentoPdfServiceImpl service = new OrcamentoPdfServiceImpl();

    @Test
    void deveGerarPdfParaOrcamentoCompleto() {
        OrcamentoEntity orcamento = OrcamentoEntity.builder()
                .id(10L)
                .ordemServicoId(1L)
                .numeroOs("OS-123")
                .cliente(clienteSnapshot())
                .veiculo(veiculoSnapshot())
                .tipo(TipoOrcamento.PRINCIPAL)
                .versao(1)
                .status(StatusOrcamento.DISPONIVEL)
                .criadoEm(LocalDateTime.of(2026, 6, 7, 10, 0))
                .disponibilizadoEm(LocalDateTime.of(2026, 6, 7, 11, 0))
                .servicos(List.of(OrcamentoServicoEntity.builder()
                        .servicoId(55L)
                        .nome("Revisao")
                        .valor(new BigDecimal("100.00"))
                        .build()))
                .itens(List.of(OrcamentoItemNecessarioEntity.builder()
                        .pecaInsumoId(20L)
                        .servicoOsId(55L)
                        .nome("Filtro")
                        .tipo(CategoriaPecaInsumo.PECA)
                        .valorUnitario(new BigDecimal("25.00"))
                        .quantidade(2)
                        .valorTotal(new BigDecimal("50.00"))
                        .build()))
                .totalServicos(new BigDecimal("100.00"))
                .totalItens(new BigDecimal("50.00"))
                .totalGeral(new BigDecimal("150.00"))
                .build();

        byte[] pdf = service.gerarPdf(orcamento);

        assertPdfValido(pdf);
    }

    @Test
    void deveGerarPdfQuandoListasDatasEValoresForemNulos() {
        OrcamentoEntity orcamento = OrcamentoEntity.builder()
                .id(10L)
                .ordemServicoId(1L)
                .numeroOs("OS-123")
                .cliente(clienteSnapshot())
                .veiculo(veiculoSnapshot())
                .tipo(TipoOrcamento.PRINCIPAL)
                .versao(1)
                .status(StatusOrcamento.DISPONIVEL)
                .servicos(null)
                .itens(null)
                .totalServicos(null)
                .totalItens(null)
                .totalGeral(null)
                .build();

        byte[] pdf = service.gerarPdf(orcamento);

        assertPdfValido(pdf);
    }

    @Test
    void deveGerarPdfQuandoListasForemVazias() {
        OrcamentoEntity orcamento = OrcamentoEntity.builder()
                .id(10L)
                .ordemServicoId(1L)
                .numeroOs("OS-123")
                .cliente(clienteSnapshot())
                .veiculo(veiculoSnapshot())
                .tipo(TipoOrcamento.PRINCIPAL)
                .versao(1)
                .status(StatusOrcamento.DISPONIVEL)
                .servicos(List.of())
                .itens(List.of())
                .totalServicos(BigDecimal.ZERO)
                .totalItens(BigDecimal.ZERO)
                .totalGeral(BigDecimal.ZERO)
                .build();

        byte[] pdf = service.gerarPdf(orcamento);

        assertPdfValido(pdf);
    }

    private void assertPdfValido(byte[] pdf) {
        assertTrue(pdf.length > 0);
        assertTrue(new String(pdf, 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
    }

    private ClienteOrcamentoSnapshot clienteSnapshot() {
        return ClienteOrcamentoSnapshot.builder()
                .nome("Cliente")
                .cpfCnpj("12345678901")
                .email("cliente@exemplo.com")
                .telefone("11999999999")
                .build();
    }

    private VeiculoOrcamentoSnapshot veiculoSnapshot() {
        return VeiculoOrcamentoSnapshot.builder()
                .placa("ABC1234")
                .marca("Fiat")
                .modelo("Uno")
                .ano(2020)
                .build();
    }
}
