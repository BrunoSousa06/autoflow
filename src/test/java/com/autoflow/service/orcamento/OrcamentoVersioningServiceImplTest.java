package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.service.orcamento.impl.OrcamentoVersioningServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoVersioningServiceImplTest {

    @InjectMocks
    private OrcamentoVersioningServiceImpl service;

    @Mock
    private OrcamentoRepository repository;

    @Test
    void deveRetornarVersao1QuandoNaoExisteAnterior() {
        when(repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(1L, TipoOrcamento.PRINCIPAL))
                .thenReturn(Optional.empty());

        assertEquals(1, service.proximaVersaoPrincipal(1L));
    }

    @Test
    void deveRetornarUltimaVersaoMais1() {
        OrcamentoEntity ultimo = new OrcamentoEntity();
        ultimo.setVersao(3);

        when(repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(1L, TipoOrcamento.PRINCIPAL))
                .thenReturn(Optional.of(ultimo));

        assertEquals(4, service.proximaVersaoPrincipal(1L));
    }

    @Test
    void deveRetornarProximaVersaoAdicional() {
        OrcamentoEntity ultimo = new OrcamentoEntity();
        ultimo.setVersao(2);

        when(repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(1L, TipoOrcamento.ADICIONAL))
                .thenReturn(Optional.of(ultimo));

        assertEquals(3, service.proximaVersaoAdicional(1L));
    }

    @Test
    void deveRetornarVersao1QuandoNaoExisteAdicionalAnterior() {
        when(repository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(1L, TipoOrcamento.ADICIONAL))
                .thenReturn(Optional.empty());

        assertEquals(1, service.proximaVersaoAdicional(1L));
    }

    @Test
    void deveSubstituirOrcamentoDisponivelAtual() {
        OrcamentoEntity atual = new OrcamentoEntity();
        atual.setStatus(StatusOrcamento.DISPONIVEL);
        when(repository.findByOrdemServicoIdAndStatus(1L, StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.of(atual));

        service.substituirDisponivelAtual(1L);

        assertEquals(StatusOrcamento.SUBSTITUIDO, atual.getStatus());
        verify(repository).saveAndFlush(atual);
    }

    @Test
    void naoDeveSalvarQuandoNaoExisteOrcamentoDisponivelAtual() {
        when(repository.findByOrdemServicoIdAndStatus(1L, StatusOrcamento.DISPONIVEL))
                .thenReturn(Optional.empty());

        service.substituirDisponivelAtual(1L);

        verify(repository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }
}

