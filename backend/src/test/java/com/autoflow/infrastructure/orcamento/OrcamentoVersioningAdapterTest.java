package com.autoflow.infrastructure.orcamento;

import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.infrastructure.persistence.entity.orcamento.OrcamentoPersistenceEntity;
import com.autoflow.infrastructure.persistence.repository.OrcamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrcamentoVersioningAdapterTest {

    @Mock
    private OrcamentoRepository repository;

    @Test
    void deveIncrementarVersaoPorIdETipo() {
        var ultimo = orcamento(3, TipoOrcamento.COMPLEMENTAR, StatusOrcamento.APROVADO);
        when(repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(10L, TipoOrcamento.COMPLEMENTAR))
                .thenReturn(Optional.of(ultimo));

        int versao = adapter().proximaVersao(10L, TipoOrcamento.COMPLEMENTAR);

        assertEquals(4, versao);
    }

    @Test
    void deveIniciarSequenciaPorIdQuandoNaoHouverOrcamento() {
        when(repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(10L, TipoOrcamento.PRINCIPAL))
                .thenReturn(Optional.empty());

        assertEquals(1, adapter().proximaVersao(10L, TipoOrcamento.PRINCIPAL));
    }

    @Test
    void deveIncrementarVersaoPorNumeroOsETipo() {
        var ultimo = orcamento(2, TipoOrcamento.PRINCIPAL, StatusOrcamento.SUBSTITUIDO);
        when(repository.findTopByNumeroOsAndTipoOrderByVersaoDesc("OS-10", TipoOrcamento.PRINCIPAL))
                .thenReturn(Optional.of(ultimo));

        assertEquals(3, adapter().proximaVersaoPorNumeroOs("OS-10", TipoOrcamento.PRINCIPAL));
    }

    @Test
    void deveSubstituirSomenteDisponivelDoMesmoTipo() {
        var atual = orcamento(1, TipoOrcamento.COMPLEMENTAR, StatusOrcamento.DISPONIVEL);
        when(repository.findByOrdemServicoIdAndTipoAndStatus(
                10L, TipoOrcamento.COMPLEMENTAR, StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.of(atual));

        adapter().substituirDisponivelAtual(10L, TipoOrcamento.COMPLEMENTAR);

        assertEquals(StatusOrcamento.SUBSTITUIDO, atual.getStatus());
        verify(repository).saveAndFlush(atual);
    }

    @Test
    void naoDeveSubstituirDisponivelDeOutroTipo() {
        var atual = orcamento(1, TipoOrcamento.PRINCIPAL, StatusOrcamento.DISPONIVEL);
        when(repository.findByOrdemServicoIdAndTipoAndStatus(
                10L, TipoOrcamento.COMPLEMENTAR, StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());

        adapter().substituirDisponivelAtual(10L, TipoOrcamento.COMPLEMENTAR);

        assertEquals(StatusOrcamento.DISPONIVEL, atual.getStatus());
        verify(repository, never()).saveAndFlush(atual);
    }

    private OrcamentoVersioningAdapter adapter() {
        return new OrcamentoVersioningAdapter(repository);
    }

    private OrcamentoPersistenceEntity orcamento(int versao, TipoOrcamento tipo, StatusOrcamento status) {
        var orcamento = new OrcamentoPersistenceEntity();
        orcamento.setVersao(versao);
        orcamento.setTipo(tipo);
        orcamento.setStatus(status);
        return orcamento;
    }
}
