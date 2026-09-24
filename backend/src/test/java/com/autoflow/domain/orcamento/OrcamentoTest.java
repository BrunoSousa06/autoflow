package com.autoflow.domain.orcamento;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class OrcamentoTest {

    private Orcamento orcamento;

    @BeforeEach
    void setUp() {
        ClienteOrcamentoSnapshot cliente = new ClienteOrcamentoSnapshot();
        cliente.setNome("João Silva");
        cliente.setCpfCnpj("12345678901");

        VeiculoOrcamentoSnapshot veiculo = new VeiculoOrcamentoSnapshot();
        veiculo.setMarca("Toyota");
        veiculo.setPlaca("ABC1234");

        orcamento = Orcamento.builder()
                .id(1L)
                .ordemServicoId(100L)
                .numeroOs("OS-001")
                .tipo(TipoOrcamento.COMPLEMENTAR)
                .versao(1)
                .status(StatusOrcamento.DISPONIVEL)
                .criadoEm(LocalDateTime.now())
                .totalServicos(new BigDecimal("500.00"))
                .totalItens(new BigDecimal("250.00"))
                .totalGeral(new BigDecimal("750.00"))
                .cliente(cliente)
                .veiculo(veiculo)
                .servicos(new ArrayList<>())
                .itens(new ArrayList<>())
                .build();
    }

    @Test
    void testOrcamentoCreation() {
        assertNotNull(orcamento);
        assertEquals(1L, orcamento.getId());
        assertEquals(100L, orcamento.getOrdemServicoId());
        assertEquals("OS-001", orcamento.getNumeroOs());
        assertEquals(TipoOrcamento.COMPLEMENTAR, orcamento.getTipo());
        assertEquals(StatusOrcamento.DISPONIVEL, orcamento.getStatus());
    }

    @Test
    void testOrcamentoValores() {
        assertEquals(new BigDecimal("500.00"), orcamento.getTotalServicos());
        assertEquals(new BigDecimal("250.00"), orcamento.getTotalItens());
        assertEquals(new BigDecimal("750.00"), orcamento.getTotalGeral());
    }

    @Test
    void testOrcamentoClienteSnapshot() {
        assertNotNull(orcamento.getCliente());
        assertEquals("João Silva", orcamento.getCliente().getNome());
        assertEquals("12345678901", orcamento.getCliente().getCpfCnpj());
    }

    @Test
    void testOrcamentoVeiculoSnapshot() {
        assertNotNull(orcamento.getVeiculo());
        assertEquals("Toyota", orcamento.getVeiculo().getMarca());
        assertEquals("ABC1234", orcamento.getVeiculo().getPlaca());
    }

    @Test
    void testOrcamentoServicosCollection() {
        assertNotNull(orcamento.getServicos());
        assertTrue(orcamento.getServicos().isEmpty());
    }

    @Test
    void testOrcamentoItensCollection() {
        assertNotNull(orcamento.getItens());
        assertTrue(orcamento.getItens().isEmpty());
    }

    @Test
    void testOrcamentoAprovacao() {
        LocalDateTime aprovadoEm = LocalDateTime.now();
        orcamento.aprovar("Maria", aprovadoEm);

        assertEquals(StatusOrcamento.APROVADO, orcamento.getStatus());
        assertEquals(aprovadoEm, orcamento.getAprovadoEm());
        assertEquals("Maria", orcamento.getAssinaturaNome());
    }

    @Test
    void testOrcamentoReprovacao() {
        LocalDateTime reprovadoEm = LocalDateTime.now();
        orcamento.recusar("Valor muito alto", "Maria", reprovadoEm);

        assertEquals(StatusOrcamento.REPROVADO, orcamento.getStatus());
        assertEquals(reprovadoEm, orcamento.getReprovadoEm());
        assertEquals("Valor muito alto", orcamento.getRecusaMotivo());
    }

    @Test
    void testOrcamentoPublicToken() {
        String tokenHash = "hash_token_123456";
        LocalDateTime disponibilizadoEm = LocalDateTime.now();
        orcamento.publicar(tokenHash, disponibilizadoEm.plusDays(7), disponibilizadoEm);

        assertEquals(tokenHash, orcamento.getPublicTokenHash());
    }

    @Test
    void testOrcamentoDisponibilizacao() {
        LocalDateTime disponibilizadoEm = LocalDateTime.now();
        orcamento.publicar("hash_token_123456", disponibilizadoEm.plusDays(7), disponibilizadoEm);
        assertNotNull(orcamento.getDisponibilizadoEm());
    }

    @Test
    void testOrcamentoNaoPodeSerAprovadoDepoisDeRecusado() {
        orcamento.recusar("Valor muito alto", "Maria", LocalDateTime.now());

        assertThrows(IllegalStateException.class,
                () -> orcamento.aprovar("Maria", LocalDateTime.now()));
    }
}
