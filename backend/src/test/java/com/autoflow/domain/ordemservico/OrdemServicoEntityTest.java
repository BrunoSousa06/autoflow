package com.autoflow.domain.ordemservico;

import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import com.autoflow.infrastructure.persistence.entity.veiculo.VeiculoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    
    @Test
    void testAdicionarServicosSolicitados_listaNula_retornaSemErro() {
        assertDoesNotThrow(() -> ordemServico.adicionarServicosSolicitados(null));
        assertTrue(ordemServico.getServicosSolicitados().isEmpty());
    }

    @Test
    void testAdicionarServicosSolicitados_listaVazia_retornaSemErro() {
        List<ServicoSolicitadoEntity> servicos = List.of();
        assertDoesNotThrow(() -> ordemServico.adicionarServicosSolicitados(servicos));
        assertTrue(ordemServico.getServicosSolicitados().isEmpty());
    }

    @Test
    void testAdicionarServicosSolicitados_statusFinalizada_lanca() {
        ordemServico.setStatus(StatusOrdemServico.FINALIZADA);
        ServicoSolicitadoEntity s = ServicoSolicitadoEntity.criar(1L, "Troca de oleo", BigDecimal.TEN);
        List<ServicoSolicitadoEntity> servicos = List.of(s);
        assertThrows(IllegalStateException.class, () -> ordemServico.adicionarServicosSolicitados(servicos));
    }

    @Test
    void testAdicionarServicosSolicitados_statusEntregue_lanca() {
        ordemServico.setStatus(StatusOrdemServico.ENTREGUE);
        ServicoSolicitadoEntity s = ServicoSolicitadoEntity.criar(1L, "Troca de oleo", BigDecimal.TEN);
        List<ServicoSolicitadoEntity> servicos = List.of(s);
        assertThrows(IllegalStateException.class, () -> ordemServico.adicionarServicosSolicitados(servicos));
    }

    @Test
    void testAdicionarServicosSolicitados_servicoSemId_lanca() {
        ServicoSolicitadoEntity s = new ServicoSolicitadoEntity();
        s.setNome("Sem ID");
        List<ServicoSolicitadoEntity> servicos = List.of(s);
        assertThrows(IllegalArgumentException.class, () -> ordemServico.adicionarServicosSolicitados(servicos));
    }

    @Test
    void testAdicionarServicosSolicitados_idDuplicadoNarequisição_lanca() {
        ServicoSolicitadoEntity s1 = ServicoSolicitadoEntity.criar(5L, "Servico A", BigDecimal.TEN);
        ServicoSolicitadoEntity s2 = ServicoSolicitadoEntity.criar(5L, "Servico A dup", BigDecimal.TEN);
        List<ServicoSolicitadoEntity> servicos = List.of(s1, s2);
        assertThrows(IllegalArgumentException.class, () -> ordemServico.adicionarServicosSolicitados(servicos));
    }

    @Test
    void testAdicionarServicosSolicitados_idJaExistenteNaOs_lanca() {
        ServicoSolicitadoEntity s1 = ServicoSolicitadoEntity.criar(7L, "Servico B", BigDecimal.TEN);
        ordemServico.adicionarServicosSolicitados(List.of(s1));

        ServicoSolicitadoEntity s2 = ServicoSolicitadoEntity.criar(7L, "Servico B novamente", BigDecimal.TEN);
        List<ServicoSolicitadoEntity> servicos = List.of(s2);
        assertThrows(IllegalArgumentException.class, () -> ordemServico.adicionarServicosSolicitados(servicos));
    }

    @Test
    void testAdicionarServicosSolicitados_sucesso_associaOrdemServico() {
        ServicoSolicitadoEntity s = ServicoSolicitadoEntity.criar(3L, "Alinhamento", BigDecimal.TEN);
        ordemServico.adicionarServicosSolicitados(List.of(s));

        assertEquals(1, ordemServico.getServicosSolicitados().size());
        assertSame(ordemServico, ordemServico.getServicosSolicitados().get(0).getOrdemServico());
    }

    // --- buscarServicoSolicitado ---

    @Test
    void testBuscarServicoSolicitado_encontrado() {
        ServicoSolicitadoEntity s = ServicoSolicitadoEntity.criar(10L, "Revisao", BigDecimal.TEN);
        ordemServico.adicionarServicosSolicitados(List.of(s));

        ServicoSolicitadoEntity encontrado = ordemServico.buscarServicoSolicitado(10L);
        assertEquals(10L, encontrado.getServicoId());
    }

    @Test
    void testBuscarServicoSolicitado_naoEncontrado_lanca() {
        assertThrows(IllegalArgumentException.class, () -> ordemServico.buscarServicoSolicitado(99L));
    }

    // --- aguardarAprovacao ---

    @Test
    void testAguardarAprovacao_sucesso() {
        ordemServico.iniciarDiagnostico();
        ordemServico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        ordemServico.aguardarAprovacao();

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordemServico.getStatus());
    }

    @Test
    void testAguardarAprovacao_transicaoInvalida_lanca() {
        assertThrows(IllegalStateException.class, () -> ordemServico.aguardarAprovacao());
    }

    // --- iniciarExecucao ---

    @Test
    void testIniciarExecucao_sucesso_defineDateAndStatus() {
        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        ordemServico.iniciarExecucao();

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordemServico.getStatus());
        assertNotNull(ordemServico.getExecucaoIniciadaEm());
    }

    @Test
    void testIniciarExecucao_transicaoInvalida_lanca() {
        assertThrows(IllegalStateException.class, () -> ordemServico.iniciarExecucao());
    }

    @Test
    void testIniciarExecucao_execucaoJaDefinida_naoReset() {
        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        LocalDateTime primeira = LocalDateTime.now().minusMinutes(5);
        ordemServico.setExecucaoIniciadaEm(primeira);
        ordemServico.iniciarExecucao();

        assertEquals(primeira, ordemServico.getExecucaoIniciadaEm());
    }

    // --- finalizarPorOrcamentoRecusado ---

    @Test
    void testFinalizarPorOrcamentoRecusado_sucesso() {
        ordemServico.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);
        ordemServico.finalizarPorOrcamentoRecusado();

        assertEquals(StatusOrdemServico.FINALIZADA, ordemServico.getStatus());
        assertNotNull(ordemServico.getFinalizadaEm());
    }

    @Test
    void testFinalizarPorOrcamentoRecusado_transicaoInvalida_lanca() {
        assertThrows(IllegalStateException.class, () -> ordemServico.finalizarPorOrcamentoRecusado());
    }

    // --- finalizarSeTodosServicosFinalizados ---

    @Test
    void testFinalizarSeTodos_statusJaFinalizada_retornaCedo() {
        ordemServico.setStatus(StatusOrdemServico.FINALIZADA);
        ordemServico.finalizarSeTodosServicosFinalizados();

        assertEquals(StatusOrdemServico.FINALIZADA, ordemServico.getStatus());
    }

    @Test
    void testFinalizarSeTodos_statusJaEntregue_retornaCedo() {
        ordemServico.setStatus(StatusOrdemServico.ENTREGUE);
        ordemServico.finalizarSeTodosServicosFinalizados();

        assertEquals(StatusOrdemServico.ENTREGUE, ordemServico.getStatus());
    }

    @Test
    void testFinalizarSeTodos_listaVazia_naoFinaliza() {
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.finalizarSeTodosServicosFinalizados();

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordemServico.getStatus());
    }

    @Test
    void testFinalizarSeTodos_nenhumFinalizado_naoFinaliza() {
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ServicoSolicitadoEntity s = ServicoSolicitadoEntity.criar(1L, "Servico", BigDecimal.TEN);
        ordemServico.adicionarServicosSolicitados(List.of(s));

        ordemServico.finalizarSeTodosServicosFinalizados();

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordemServico.getStatus());
    }

    @Test
    void testFinalizarSeTodos_todosFinalizados_finalizaOs() {
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ServicoSolicitadoEntity s = ServicoSolicitadoEntity.criar(1L, "Servico", BigDecimal.TEN);
        ordemServico.adicionarServicosSolicitados(List.of(s));
        s.setStatus(StatusServicoOs.FINALIZADO);

        ordemServico.finalizarSeTodosServicosFinalizados();

        assertEquals(StatusOrdemServico.FINALIZADA, ordemServico.getStatus());
        assertNotNull(ordemServico.getFinalizadaEm());
    }

    @Test
    void testFinalizarSeTodos_todosCancelados_finalizaOs() {
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ServicoSolicitadoEntity s = ServicoSolicitadoEntity.criar(2L, "Servico", BigDecimal.TEN);
        ordemServico.adicionarServicosSolicitados(List.of(s));
        s.setStatus(StatusServicoOs.CANCELADO);

        ordemServico.finalizarSeTodosServicosFinalizados();

        assertEquals(StatusOrdemServico.FINALIZADA, ordemServico.getStatus());
    }

    // --- entregar ---

    @Test
    void testEntregar_sucesso() {
        ordemServico.setStatus(StatusOrdemServico.FINALIZADA);
        ordemServico.entregar();

        assertEquals(StatusOrdemServico.ENTREGUE, ordemServico.getStatus());
        assertNotNull(ordemServico.getEntregueEm());
    }

    @Test
    void testEntregar_transicaoInvalida_lanca() {
        assertThrows(IllegalStateException.class, () -> ordemServico.entregar());
    }

    // --- validaSePodeFinalizarDiagnostico ---

    @Test
    void testFinalizarDiagnostico_semDiagnostico_lanca() {
        ordemServico.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        assertThrows(IllegalArgumentException.class, () -> ordemServico.finalizarDiagnostico());
    }

    @Test
    void testFinalizarDiagnostico_semLaudo_lanca() {
        ordemServico.iniciarDiagnostico();
        assertThrows(Exception.class, () -> ordemServico.finalizarDiagnostico());
    }

    @Test
    void testFinalizarDiagnostico_statusErrado_lanca() {
        assertThrows(IllegalArgumentException.class, () -> ordemServico.finalizarDiagnostico());
    }

    // --- iniciarDiagnostico com diagnostico ja existente ---

    @Test
    void testIniciarDiagnostico_comDiagnosticoExistente_naoRecriaObjeto() {
        ordemServico.iniciarDiagnostico();
        DiagnosticoEntity primeiro = ordemServico.getDiagnostico();
        ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
        ordemServico.iniciarDiagnostico();

        assertNotNull(ordemServico.getDiagnostico());
        assertSame(primeiro, ordemServico.getDiagnostico());
    }

    @Test
    void deveCriarOrdemServicoComDadosInternosDoCliente() {
        OrdemServicoEntity resultado = OrdemServicoEntity.criar(
                1L, "Joao Silva", "12345678901", "joao@example.com", null, veiculo);

        assertEquals(1L, resultado.getCliente().getId());
        assertEquals("Joao Silva", resultado.getCliente().getNome());
    }

    @Test
    void deveRejeitarDadosInvalidosDoClienteInterno() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(
                        null, "Joao Silva", "12345678901", "joao@example.com", null, veiculo)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(
                        1L, null, "12345678901", "joao@example.com", null, veiculo)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(
                        1L, "", "12345678901", "joao@example.com", null, veiculo)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(
                        1L, "Joao Silva", null, "joao@example.com", null, veiculo)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(
                        1L, "Joao Silva", "", "joao@example.com", null, veiculo)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(
                        1L, "Joao Silva", "12345678901", null, null, veiculo)),
                () -> assertThrows(IllegalArgumentException.class, () -> OrdemServicoEntity.criar(
                        1L, "Joao Silva", "12345678901", "", null, veiculo)));
    }

    @Test
    void deveValidarDadosDoClienteAoCriarSnapshotDaOs() {
        ClienteEntity semId = clienteValido();
        semId.setId(null);
        ClienteEntity nomeNulo = clienteValido();
        nomeNulo.setNome(null);
        ClienteEntity nomeEmBranco = clienteValido();
        nomeEmBranco.setNome(" ");
        ClienteEntity cpfNulo = clienteValido();
        cpfNulo.setCpfCnpj(null);
        ClienteEntity cpfEmBranco = clienteValido();
        cpfEmBranco.setCpfCnpj(" ");
        ClienteEntity emailNulo = clienteValido();
        emailNulo.setEmail(null);
        ClienteEntity emailEmBranco = clienteValido();
        emailEmBranco.setEmail(" ");

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ClienteOsEntity.fromCliente(null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ClienteOsEntity.fromCliente(semId)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ClienteOsEntity.fromCliente(nomeNulo)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ClienteOsEntity.fromCliente(nomeEmBranco)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ClienteOsEntity.fromCliente(cpfNulo)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ClienteOsEntity.fromCliente(cpfEmBranco)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ClienteOsEntity.fromCliente(emailNulo)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ClienteOsEntity.fromCliente(emailEmBranco))
        );
    }

    @Test
    void testAcompanhamentoPublicoIndisponivelSemToken() {
        assertFalse(ordemServico.acompanhamentoPublicoDisponivel(LocalDateTime.now()));
    }

    @Test
    void testAcompanhamentoPublicoDisponivelComTokenSemExpiracao() {
        LocalDateTime criadoEm = LocalDateTime.now().minusMinutes(1);
        ordemServico.configurarAcompanhamentoPublico("hash-token", criadoEm, null);

        assertTrue(ordemServico.acompanhamentoPublicoDisponivel(LocalDateTime.now()));
    }

    @Test
    void deveValidarDadosAoConfigurarTokenDeAcompanhamento() {
        LocalDateTime criadoEm = LocalDateTime.now();
        LocalDateTime expiradaEm = criadoEm.minusSeconds(1);

        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ordemServico.configurarAcompanhamentoPublico(null, criadoEm, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ordemServico.configurarAcompanhamentoPublico(" ", criadoEm, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ordemServico.configurarAcompanhamentoPublico("hash", null, null)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ordemServico.configurarAcompanhamentoPublico("hash", criadoEm, criadoEm)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> ordemServico.configurarAcompanhamentoPublico(
                                "hash", criadoEm, expiradaEm))
        );
    }

    @Test
    void deveIndisponibilizarTokenRevogadoOuExpirado() {
        LocalDateTime agora = LocalDateTime.now();
        ordemServico.configurarAcompanhamentoPublico("hash", agora.minusMinutes(2), agora.plusMinutes(1));
        assertTrue(ordemServico.acompanhamentoPublicoDisponivel(agora));
        ordemServico.setAcompanhamentoTokenRevogadoEm(agora.minusMinutes(1));
        assertFalse(ordemServico.acompanhamentoPublicoDisponivel(agora));

        ordemServico.setAcompanhamentoTokenRevogadoEm(null);
        ordemServico.configurarAcompanhamentoPublico("hash", agora.minusMinutes(2), agora.minusMinutes(1));
        assertFalse(ordemServico.acompanhamentoPublicoDisponivel(agora));
    }

    private ClienteEntity clienteValido() {
        ClienteEntity resultado = new ClienteEntity();
        resultado.setId(1L);
        resultado.setNome("Joao Silva");
        resultado.setCpfCnpj("12345678901");
        resultado.setEmail("joao@example.com");
        return resultado;
    }

    // --- equals e hashCode ---

    @Test
    void testEquals_mesmoObjeto_true() {
        assertEquals(ordemServico, ordemServico);
    }

    @Test
    void testEquals_null_false() {
        assertNotEquals(ordemServico, null);
    }

    @Test
    void testEquals_classeDiferente_false() {
        assertNotEquals(ordemServico, "outro tipo");
    }

    @Test
    void testEquals_idNulo_false() {
        OrdemServicoEntity semId = new OrdemServicoEntity();
        assertNotEquals(ordemServico, semId);
        assertNotEquals(semId, ordemServico);
    }

    @Test
    void testEquals_mesmoId_true() {
        OrdemServicoEntity outro = new OrdemServicoEntity();
        ordemServico.setId(42L);
        outro.setId(42L);
        assertEquals(outro, ordemServico);
    }

    @Test
    void testEquals_idsDiferentes_false() {
        OrdemServicoEntity outro = new OrdemServicoEntity();
        ordemServico.setId(1L);
        outro.setId(2L);
        assertNotEquals(outro, ordemServico);
    }

    @Test
    void testHashCode_instanciaNormal() {
        assertDoesNotThrow(() -> ordemServico.hashCode());
    }
}
