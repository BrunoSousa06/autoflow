package com.autoflow.service.orcamento;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoVersioningServiceTest {

    @InjectMocks
    private OrcamentoVersioningService service;

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
}

