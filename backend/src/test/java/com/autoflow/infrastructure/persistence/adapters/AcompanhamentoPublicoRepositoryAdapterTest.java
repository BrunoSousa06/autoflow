package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.exception.OrdemServicoNaoEncontradaException;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.ordemservico.acompanhamento.AcessoAcompanhamento;
import com.autoflow.infrastructure.persistence.repository.OrcamentoRepository;
import com.autoflow.infrastructure.persistence.repository.OrdemServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcompanhamentoPublicoRepositoryAdapterTest {

    @Mock OrdemServicoRepository ordemServicoRepository;
    @Mock OrcamentoRepository orcamentoRepository;

    private AcompanhamentoPublicoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AcompanhamentoPublicoRepositoryAdapter(ordemServicoRepository, orcamentoRepository);
    }

    @Test
    void deveSalvarAcessoNaOrdemServico() {
        var ordemServico = novaOrdemServico();
        var criadoEm = LocalDateTime.of(2026, Month.AUGUST, 2, 10, 0);
        var acesso = new AcessoAcompanhamento("hash-token", criadoEm, criadoEm.plusDays(1), null);
        when(ordemServicoRepository.findById(1L)).thenReturn(Optional.of(ordemServico));

        adapter.salvar(1L, acesso);

        assertEquals("hash-token", ordemServico.getAcompanhamentoTokenHash());
        assertEquals(criadoEm, ordemServico.getAcompanhamentoTokenCriadoEm());
        assertEquals(criadoEm.plusDays(1), ordemServico.getAcompanhamentoTokenExpiraEm());
        verify(ordemServicoRepository).save(ordemServico);
    }

    @Test
    void deveFalharAoSalvarQuandoOrdemServicoNaoExistir() {
        var acesso = new AcessoAcompanhamento("hash-token", LocalDateTime.now(), null, null);
        when(ordemServicoRepository.findById(99L)).thenReturn(Optional.empty());

        var exception = assertThrows(
                OrdemServicoNaoEncontradaException.class,
                () -> adapter.salvar(99L, acesso)
        );

        assertTrue(exception.getMessage().contains("99"));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveBuscarDadosComIdDoOrcamentoDisponivel() {
        var ordemServico = novaOrdemServico();
        var orcamento = new OrcamentoEntity();
        orcamento.setId(10L);
        orcamento.setStatus(StatusOrcamento.DISPONIVEL);
        when(ordemServicoRepository.findByAcompanhamentoTokenHash("hash-token"))
                .thenReturn(Optional.of(ordemServico));
        when(orcamentoRepository.findTopByNumeroOsOrderByVersaoDesc("OS-123"))
                .thenReturn(Optional.of(orcamento));

        var resultado = adapter.buscarPorTokenHash("hash-token").orElseThrow();

        assertEquals("OS-123", resultado.numeroOs());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, resultado.status());
        assertEquals(10L, resultado.orcamentoId());
        assertEquals("hash-token", resultado.acesso().tokenHash());
    }

    @Test
    void deveOmitirOrcamentoQuandoUltimoNaoEstiverDisponivel() {
        var ordemServico = novaOrdemServico();
        var orcamento = new OrcamentoEntity();
        orcamento.setId(10L);
        orcamento.setStatus(StatusOrcamento.APROVADO);
        when(ordemServicoRepository.findByAcompanhamentoTokenHash("hash-token"))
                .thenReturn(Optional.of(ordemServico));
        when(orcamentoRepository.findTopByNumeroOsOrderByVersaoDesc("OS-123"))
                .thenReturn(Optional.of(orcamento));

        var resultado = adapter.buscarPorTokenHash("hash-token").orElseThrow();

        assertNull(resultado.orcamentoId());
    }

    @Test
    void deveRetornarVazioQuandoTokenNaoExistir() {
        when(ordemServicoRepository.findByAcompanhamentoTokenHash("inexistente"))
                .thenReturn(Optional.empty());

        assertTrue(adapter.buscarPorTokenHash("inexistente").isEmpty());
        verifyNoInteractions(orcamentoRepository);
    }

    private OrdemServicoEntity novaOrdemServico() {
        var criadoEm = LocalDateTime.of(2026, Month.AUGUST, 2, 10, 0);
        var ordemServico = new OrdemServicoEntity();
        ordemServico.setNumeroOs("OS-123");
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        ordemServico.setDataAbertura(criadoEm.minusDays(1));
        ordemServico.setExecucaoIniciadaEm(criadoEm.minusHours(1));
        ordemServico.configurarAcompanhamentoPublico("hash-token", criadoEm, criadoEm.plusDays(1));
        return ordemServico;
    }
}
