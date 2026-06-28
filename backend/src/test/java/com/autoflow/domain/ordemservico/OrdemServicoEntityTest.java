package com.autoflow.domain.ordemservico;

import com.autoflow.domain.cliente.ClienteEntity;
import com.autoflow.domain.veiculo.VeiculoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoEntityTest {

    private OrdemServicoEntity ordemServico;
    private VeiculoEntity veiculo;
    private ClienteEntity cliente;

    @BeforeEach
    void setUp() {
        cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("joao@example.com");

        veiculo = new VeiculoEntity();
        veiculo.setId(1L);
        veiculo.setMarca("Toyota");
        veiculo.setCliente(cliente);

        ordemServico = OrdemServicoEntity.criar(cliente, veiculo);
    }

    @Test
    void testOrdemServicoCreation() {
        assertNotNull(ordemServico);
        assertNotNull(ordemServico.getNumeroOs());
        assertEquals(StatusOrdemServico.RECEBIDA, ordemServico.getStatus());
        assertNotNull(ordemServico.getDataAbertura());
    }

    @Test
    void testOrdemServicoNumeracao() {
        OrdemServicoEntity os2 = OrdemServicoEntity.criar(cliente, veiculo);
        assertNotEquals(ordemServico.getNumeroOs(), os2.getNumeroOs());
    }

    @Test
    void testIniciarDiagnostico() {
        ordemServico.iniciarDiagnostico();

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordemServico.getStatus());
        assertNotNull(ordemServico.getDiagnostico());
        assertNotNull(ordemServico.getDiagnostico().getIniciadoEm());
    }

    @Test
    void testRegistrarLaudo() {
        ordemServico.iniciarDiagnostico();
        ordemServico.registrarLaudo("Laudo: Motor em perfeito estado");

        assertEquals("Laudo: Motor em perfeito estado", ordemServico.getDiagnostico().getLaudo());
    }

    @Test
    void testRegistrarLaudoSemDiagnostico() {
        assertThrows(IllegalArgumentException.class, () -> ordemServico.registrarLaudo("Laudo teste"));
    }

    @Test
    void testFinalizarDiagnostico() {
        ordemServico.iniciarDiagnostico();
        ordemServico.registrarLaudo("Laudo teste");
        ordemServico.finalizarDiagnostico();

        assertNotNull(ordemServico.getDiagnostico().getConcluidoEm());
    }

    @Test
    void testAtualizarUltimaAtualizacao() {
        LocalDateTime antes = ordemServico.getUltimaAtualizacao();
        ordemServico.atualizarUltimaAtualizacao();
        LocalDateTime depois = ordemServico.getUltimaAtualizacao();

        assertTrue(depois.isAfter(antes) || depois.isEqual(antes));
    }

    @Test
    void testOrdemServicoVeiculoAssociation() {
        assertNotNull(ordemServico.getVeiculo());
        assertEquals("Toyota", ordemServico.getVeiculo().getMarca());
    }

    @Test
    void testOrdemServicoClienteAssociation() {
        assertNotNull(ordemServico.getCliente());
        assertEquals("João Silva", ordemServico.getCliente().getNome());
    }

    @Test
    void testOrdemServicoServicosCollection() {
        assertNotNull(ordemServico.getServicosSolicitados());
        assertTrue(ordemServico.getServicosSolicitados().isEmpty());
    }

    @Test
    void testOrdemServicoInvalidVeiculo() {
        assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(cliente, null));
    }

    @Test
    void testOrdemServicoInvalidCliente() {
        veiculo.setCliente(null);
        assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(null, veiculo));
    }
}
