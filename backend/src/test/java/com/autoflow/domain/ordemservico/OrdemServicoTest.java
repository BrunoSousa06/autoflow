package com.autoflow.domain.ordemservico;

import com.autoflow.domain.veiculo.Veiculo;

import com.autoflow.domain.cliente.Cliente;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 8, 18, 12, 30);

    @Test
    void deveCriarEAvancarUmaOrdemServico() {
        Cliente cliente = Cliente.reconstituir(1L, "Cliente", "123", "11999999999", "cliente@email.com");
        OrdemServico ordem = OrdemServico.criar(cliente, new Veiculo(2L, "ABC1D23", "Honda", "Civic", 2020), "OS-1", AGORA);
        ordem.adicionarServicosSolicitados(List.of(ServicoSolicitado.criar(10L, "Revisao", BigDecimal.TEN)));

        ordem.iniciarDiagnostico(AGORA);
        ordem.registrarLaudo("Laudo", AGORA);
        ordem.finalizarDiagnostico(AGORA);
        ordem.aguardarAprovacao(AGORA);
        ordem.iniciarExecucao(AGORA);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
        assertEquals(1L, ordem.getClienteId());
        assertEquals("ABC1D23", ordem.getVeiculoPlaca());
    }

    @Test
    void deveRecusarTransicaoInvalida() {
        OrdemServico ordem = OrdemServico.criar(
                Cliente.reconstituir(1L, "Cliente", "123", null, "cliente@email.com"),
                new Veiculo(2L, "ABC1D23", null, null, null), "OS-2", AGORA);

        assertThrows(IllegalStateException.class, () -> ordem.entregar(AGORA));
    }

    @Test
    void deveCriarOrdemComIdentificadoresEDataDeAbertura() {
        OrdemServico ordem = OrdemServico.criar(
                Cliente.reconstituir(1L, "Cliente", "123", null, "cliente@email.com"),
                new Veiculo(2L, "ABC1D23", "Honda", "Civic", 2020), "OS-3", AGORA);

        assertNotNull(ordem.getNumeroOs());
        assertTrue(ordem.getNumeroOs().startsWith("OS-"));
        assertNotNull(ordem.getDataAbertura());
        assertNotNull(ordem.getUltimaAtualizacao());
    }

    @Test
    void devePreservarNumeroEDataAoReconstituir() {
        LocalDateTime abertura = LocalDateTime.of(2026, 8, 18, 12, 30);

        OrdemServico ordem = OrdemServico.reconstituir(
                10L,
                "OS-20260818-10",
                ClienteOs.fromFields(1L, "Cliente", "123", "cliente@email.com", null),
                new Veiculo(2L, "ABC1D23", "Honda", "Civic", 2020),
                StatusOrdemServico.RECEBIDA,
                abertura,
                null,
                List.of(),
                null,
                null,
                null,
                abertura,
                null,
                null,
                null,
                null);

        assertEquals("OS-20260818-10", ordem.getNumeroOs());
        assertEquals(abertura, ordem.getDataAbertura());
    }
}
