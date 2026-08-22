package com.autoflow.domain.ordemservico;

import com.autoflow.domain.cliente.Cliente;
import com.autoflow.domain.veiculo.Veiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    void deveValidarDadosObrigatoriosNaCriacaoEAtualizacao() {
        Veiculo veiculo = new Veiculo(2L, "ABC1D23", "Honda", "Civic", 2020);
        Cliente cliente = Cliente.reconstituir(1L, "Cliente", "123", null, "cliente@email.com");

        assertThrows(IllegalArgumentException.class, () -> OrdemServico.criar(null, veiculo, "OS-1", AGORA));
        assertThrows(IllegalArgumentException.class, () -> OrdemServico.criar(cliente, null, "OS-1", AGORA));
        assertThrows(IllegalArgumentException.class, () -> OrdemServico.criar(cliente, veiculo, " ", AGORA));
        assertThrows(IllegalArgumentException.class, () -> OrdemServico.criar(cliente, veiculo, "OS-1", null));
        assertThrows(IllegalArgumentException.class, () -> OrdemServico.criar(
                1L, "Cliente", "123", null, null, veiculo, "OS-1", null));

        OrdemServico ordem = OrdemServico.criar(cliente, veiculo, "OS-1", AGORA);
        assertThrows(IllegalArgumentException.class, () -> ordem.atualizarUltimaAtualizacao(null));
    }

    @Test
    void deveValidarDiagnosticoAntesDeRegistrarOuFinalizar() {
        OrdemServico ordem = ordem(StatusOrdemServico.RECEBIDA, null, List.of());

        assertThrows(IllegalArgumentException.class, () -> ordem.registrarLaudo("Laudo", AGORA));
        assertThrows(IllegalArgumentException.class, () -> ordem.finalizarDiagnostico(AGORA));

        ordem.iniciarDiagnostico(AGORA);
        assertThrows(IllegalArgumentException.class, () -> ordem.finalizarDiagnostico(AGORA));
        assertThrows(IllegalArgumentException.class, () -> ordem.registrarLaudo("Laudo", null));
        ordem.registrarLaudo("Laudo", AGORA);
        ordem.finalizarDiagnostico(AGORA);
        assertEquals(AGORA, ordem.getDiagnostico().getConcluidoEm());
    }

    @Test
    void deveControlarServicosSolicitadosSemDuplicidade() {
        OrdemServico ordem = ordem(StatusOrdemServico.RECEBIDA, null, List.of());
        ServicoSolicitado primeiro = ServicoSolicitado.criar(10L, "Revisao", BigDecimal.TEN);

        ordem.adicionarServicosSolicitados(null);
        ordem.adicionarServicosSolicitados(List.of());
        ordem.adicionarServicosSolicitados(List.of(primeiro));

        assertThrows(IllegalArgumentException.class, () -> ordem.adicionarServicosSolicitados(
                List.of(new ServicoSolicitado())));
        assertThrows(IllegalArgumentException.class, () -> ordem.adicionarServicosSolicitados(List.of(
                ServicoSolicitado.criar(20L, "Troca", BigDecimal.ONE),
                ServicoSolicitado.criar(20L, "Troca", BigDecimal.ONE))));
        assertThrows(IllegalArgumentException.class, () -> ordem.adicionarServicosSolicitados(List.of(
                ServicoSolicitado.criar(10L, "Revisao", BigDecimal.TEN))));
        assertSame(primeiro, ordem.buscarServicoSolicitado(10L));
        assertThrows(IllegalArgumentException.class, () -> ordem.buscarServicoSolicitado(99L));

        ordem.setStatus(StatusOrdemServico.FINALIZADA);
        assertThrows(IllegalStateException.class, () -> ordem.adicionarServicosSolicitados(List.of(
                ServicoSolicitado.criar(30L, "Alinhamento", BigDecimal.ONE))));
    }

    @Test
    void deveFinalizarQuandoTodosOsServicosEstiveremConcluidosOuCancelados() {
        ServicoSolicitado finalizado = ServicoSolicitado.criar(10L, "Revisao", BigDecimal.TEN);
        finalizado.setStatus(StatusServicoOs.FINALIZADO);
        ServicoSolicitado cancelado = ServicoSolicitado.criar(20L, "Troca", BigDecimal.ONE);
        cancelado.setStatus(StatusServicoOs.CANCELADO);
        OrdemServico ordem = ordem(StatusOrdemServico.EM_EXECUCAO, null, List.of(finalizado, cancelado));

        ordem.finalizarSeTodosServicosFinalizados(AGORA);

        assertEquals(StatusOrdemServico.FINALIZADA, ordem.getStatus());
        assertEquals(AGORA, ordem.getFinalizadaEm());

        ServicoSolicitado aguardando = ServicoSolicitado.criar(30L, "Diagnostico", BigDecimal.ONE);
        OrdemServico pendente = ordem(StatusOrdemServico.EM_EXECUCAO, null, List.of(aguardando));
        pendente.finalizarSeTodosServicosFinalizados(AGORA);
        assertEquals(StatusOrdemServico.EM_EXECUCAO, pendente.getStatus());

        OrdemServico semServicos = ordem(StatusOrdemServico.EM_EXECUCAO, null, List.of());
        semServicos.finalizarSeTodosServicosFinalizados(AGORA);
        assertEquals(StatusOrdemServico.EM_EXECUCAO, semServicos.getStatus());
    }

    @Test
    void deveConfigurarEValidarAcompanhamentoPublico() {
        OrdemServico ordem = ordem(StatusOrdemServico.RECEBIDA, null, List.of());

        assertThrows(IllegalArgumentException.class, () -> ordem.configurarAcompanhamentoPublico(null, AGORA, null));
        assertThrows(IllegalArgumentException.class, () -> ordem.configurarAcompanhamentoPublico(" ", AGORA, null));
        assertThrows(IllegalArgumentException.class, () -> ordem.configurarAcompanhamentoPublico("hash", null, null));
        assertThrows(IllegalArgumentException.class, () -> ordem.configurarAcompanhamentoPublico("hash", AGORA, AGORA));

        assertFalse(ordem.acompanhamentoPublicoDisponivel(AGORA));
        ordem.configurarAcompanhamentoPublico("hash", AGORA, null);
        assertTrue(ordem.acompanhamentoPublicoDisponivel(AGORA));

        ordem.configurarAcompanhamentoPublico("hash", AGORA, AGORA.plusDays(1));
        assertTrue(ordem.acompanhamentoPublicoDisponivel(AGORA));
        ordem.setAcompanhamentoTokenRevogadoEm(AGORA);
        assertFalse(ordem.acompanhamentoPublicoDisponivel(AGORA));
        ordem.setAcompanhamentoTokenRevogadoEm(null);
        assertFalse(ordem.acompanhamentoPublicoDisponivel(AGORA.plusDays(2)));
    }

    @Test
    void deveTratarVeiculoNuloEIgualdadePorId() {
        OrdemServico semVeiculo = OrdemServico.reconstituir(
                null, "OS-1", ClienteOs.fromFields(1L, "Cliente", "123", "cliente@email.com", null), null,
                StatusOrdemServico.RECEBIDA, AGORA, null, List.of(), null, null, null, AGORA,
                null, null, null, null);
        assertNull(semVeiculo.getVeiculoId());
        assertNull(semVeiculo.getVeiculoPlaca());
        assertFalse(semVeiculo.equals(null));
        assertFalse(semVeiculo.equals("OS-1"));
        assertTrue(semVeiculo.equals(semVeiculo));

        OrdemServico primeira = ordem(StatusOrdemServico.RECEBIDA, null, List.of());
        OrdemServico segunda = ordem(StatusOrdemServico.RECEBIDA, null, List.of());
        primeira.setId(null);
        segunda.setId(null);
        assertFalse(primeira.equals(segunda));
        primeira.setId(1L);
        segunda.setId(1L);
        assertTrue(primeira.equals(segunda));
        segunda.setId(2L);
        assertFalse(primeira.equals(segunda));
    }

    private static OrdemServico ordem(StatusOrdemServico status, Diagnostico diagnostico,
                                      List<ServicoSolicitado> servicos) {
        return OrdemServico.reconstituir(
                1L,
                "OS-1",
                ClienteOs.fromFields(1L, "Cliente", "123", "cliente@email.com", null),
                new Veiculo(2L, "ABC1D23", "Honda", "Civic", 2020),
                status,
                AGORA,
                diagnostico,
                servicos,
                null,
                null,
                null,
                AGORA,
                null,
                null,
                null,
                null);
    }
}
