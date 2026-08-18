package com.autoflow.domain.ordemservico;

import com.autoflow.domain.veiculo.Veiculo;

import com.autoflow.domain.cliente.Cliente;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {

    @Test
    void deveCriarEAvancarUmaOrdemServico() {
        Cliente cliente = Cliente.reconstituir(1L, "Cliente", "123", "11999999999", "cliente@email.com");
        OrdemServico ordem = OrdemServico.criar(cliente, new Veiculo(2L, "ABC1D23", "Honda", "Civic", 2020));
        ordem.adicionarServicosSolicitados(List.of(ServicoSolicitado.criar(10L, "Revisao", BigDecimal.TEN)));

        ordem.iniciarDiagnostico();
        ordem.registrarLaudo("Laudo");
        ordem.finalizarDiagnostico();
        ordem.aguardarAprovacao();
        ordem.iniciarExecucao();

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
        assertEquals(1L, ordem.getClienteId());
        assertEquals("ABC1D23", ordem.getVeiculoPlaca());
    }

    @Test
    void deveRecusarTransicaoInvalida() {
        OrdemServico ordem = OrdemServico.criar(
                Cliente.reconstituir(1L, "Cliente", "123", null, "cliente@email.com"),
                new Veiculo(2L, "ABC1D23", null, null, null));

        assertThrows(IllegalStateException.class, ordem::entregar);
    }

    @Test
    void deveCriarOrdemComIdentificadoresEDataDeAbertura() {
        OrdemServico ordem = OrdemServico.criar(
                Cliente.reconstituir(1L, "Cliente", "123", null, "cliente@email.com"),
                new Veiculo(2L, "ABC1D23", "Honda", "Civic", 2020));

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
