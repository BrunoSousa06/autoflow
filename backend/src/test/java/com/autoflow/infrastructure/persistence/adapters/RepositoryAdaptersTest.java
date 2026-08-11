package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.dto.PageQuery;
import com.autoflow.application.dto.ordemservico.OrdemServicoFiltroInput;
import com.autoflow.application.dto.pecainsumo.EstoqueItemOutput;
import com.autoflow.application.dto.pecainsumo.PecaInsumoFiltro;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.repository.*;
import com.autoflow.infrastructure.persistence.repository.historico.HistoricoStatusOsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryAdaptersTest {

    @Mock PecaInsumoRepository pecaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock HistoricoStatusOsRepository historicoRepository;
    @Mock OrcamentoRepository orcamentoRepository;
    @Mock OrdemServicoRepository ordemRepository;
    @Mock ServicoSolicitadoRepository servicoSolicitadoRepository;

    @Test
    void pecaAdapterDeveDelegarTodasOperacoes() {
        var adapter = new PecaInsumoAdapter(pecaRepository);
        var entity = new PecaInsumoEntity();
        entity.setId(1L);
        entity.setTipo(CategoriaPecaInsumo.PECA);


        when(pecaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(pecaRepository.findByNomeIgnoreCase("Filtro")).thenReturn(Optional.of(entity));
        when(pecaRepository.save(entity)).thenReturn(entity);
        when(pecaRepository.findAll()).thenReturn(List.of(entity));
        when(pecaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(pecaRepository.existsById(1L)).thenReturn(true);
        when(pecaRepository.findAllById(List.of(1L))).thenReturn(List.of(entity));
        when(pecaRepository.findAllByIdForUpdate(List.of(1L))).thenReturn(List.of(entity));
        var estoque = new EstoqueItemOutput(1L, null, CategoriaPecaInsumo.PECA, null, 0);

        assertEquals(Optional.of(entity), adapter.findById(1L));
        assertEquals(Optional.of(entity), adapter.findByNomeIgnoreCase("Filtro"));
        assertSame(entity, adapter.save(entity));
        assertEquals(List.of(entity), adapter.findAll());
        assertEquals(List.of(entity), adapter.findAll(new PecaInsumoFiltro(null, null), new PageQuery(0, 10)).content());
        assertTrue(adapter.existsById(1L));
        assertEquals(List.of(estoque), adapter.findAllById(List.of(1L)));
        assertEquals(List.of(estoque), adapter.findAllByIdForUpdate(List.of(1L)));
        adapter.deleteById(1L);
        adapter.saveAll(List.of(estoque));

        verify(pecaRepository).deleteById(1L);
        verify(pecaRepository).saveAll(List.of(entity));
    }

    @Test
    void pecaAdapterNaoDevePersistirListaDeEstoqueVazia() {
        var adapter = new PecaInsumoAdapter(pecaRepository);

        adapter.saveAll(List.of());

        verifyNoInteractions(pecaRepository);
    }

    @Test
    void pecaAdapterDeveFalharQuandoEstoqueNaoExistir() {
        var adapter = new PecaInsumoAdapter(pecaRepository);
        var estoque = new EstoqueItemOutput(9L, "Item", CategoriaPecaInsumo.PECA, null, 1);
        when(pecaRepository.findAllById(List.of(9L))).thenReturn(List.of());
        var itens = List.of(estoque);

        assertThrows(IllegalStateException.class, () -> adapter.saveAll(itens));

        verify(pecaRepository, never()).saveAll(any());
    }

    @Test
    void usuarioAdapterDeveDelegarConsultas() {
        var adapter = new UsuarioRepositoryAdapter(usuarioRepository);
        var usuario = new UsuarioEntity();
        usuario.setRole(RoleEnum.MECANICO);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioRepository.findByRole(RoleEnum.MECANICO)).thenReturn(List.of(usuario));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("mecanico@email.com")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmail("mecanico@email.com")).thenReturn(true);

        assertEquals(List.of(usuario), adapter.findAll());
        assertEquals(List.of(usuario), adapter.findByRole(RoleEnum.MECANICO));
        assertEquals(Optional.of(usuario), adapter.findById(1L));
        assertEquals(Optional.of(usuario), adapter.findByEmail("mecanico@email.com"));
        assertTrue(adapter.existsByEmail("mecanico@email.com"));
    }

    @Test
    void historicoAdapterDeveDelegarPersistenciaEOrdenacao() {
        var adapter = new HistoricoStatusOsRepositoryAdapter(historicoRepository);
        var historico = new HistoricoStatusOsEntity();
        when(historicoRepository.save(historico)).thenReturn(historico);
        when(historicoRepository.findByOrdemServicoIdOrderByRegistradoEmAsc(1L))
                .thenReturn(List.of(historico));
        when(historicoRepository.findByNumeroOsOrderByRegistradoEmAsc("OS-1"))
                .thenReturn(List.of(historico));

        assertSame(historico, adapter.save(historico));
        assertEquals(List.of(historico), adapter.findByOrdemServicoIdOrderByRegistradoEmAsc(1L));
        assertEquals(List.of(historico), adapter.findByNumeroOsOrderByRegistradoEmAsc("OS-1"));
    }

    @Test
    void orcamentoAdapterDeveDelegarTodasConsultas() {
        var adapter = new OrcamentoRepositoryAdapter(orcamentoRepository);
        var orcamento = new OrcamentoEntity();
        var esperado = Optional.of(orcamento);
        when(orcamentoRepository.save(orcamento)).thenReturn(orcamento);
        when(orcamentoRepository.findById(1L)).thenReturn(esperado);
        when(orcamentoRepository.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(1L, TipoOrcamento.PRINCIPAL))
                .thenReturn(esperado);
        when(orcamentoRepository.findTopByNumeroOsAndTipoOrderByVersaoDesc("OS-1", TipoOrcamento.PRINCIPAL))
                .thenReturn(esperado);
        when(orcamentoRepository.findByOrdemServicoIdAndStatus(1L, StatusOrcamento.APROVADO))
                .thenReturn(esperado);
        when(orcamentoRepository.findByNumeroOsAndStatus("OS-1", StatusOrcamento.APROVADO))
                .thenReturn(esperado);
        when(orcamentoRepository.findTopByNumeroOsOrderByVersaoDesc("OS-1")).thenReturn(esperado);

        assertSame(orcamento, adapter.save(orcamento));
        assertEquals(esperado, adapter.findById(1L));
        assertEquals(esperado, adapter.findTopByOrdemServicoIdAndTipoOrderByVersaoDesc(1L, TipoOrcamento.PRINCIPAL));
        assertEquals(esperado, adapter.findTopByNumeroOsAndTipoOrderByVersaoDesc("OS-1", TipoOrcamento.PRINCIPAL));
        assertEquals(esperado, adapter.findByOrdemServicoIdAndStatus(1L, StatusOrcamento.APROVADO));
        assertEquals(esperado, adapter.findByNumeroOsAndStatus("OS-1", StatusOrcamento.APROVADO));
        assertEquals(esperado, adapter.findTopByNumeroOsOrderByVersaoDesc("OS-1"));
    }

    @Test
    void ordemServicoAdapterDeveDelegarTodasConsultas() {
        var adapter = new OrdemServicoRepositoryAdapter(ordemRepository);
        var ordem = new OrdemServicoEntity();
        var esperado = Optional.of(ordem);


        when(ordemRepository.save(ordem)).thenReturn(ordem);
        when(ordemRepository.findById(1L)).thenReturn(esperado);
        when(ordemRepository.findByNumeroOs("OS-1")).thenReturn(esperado);
        when(ordemRepository.findByNumeroOsForUpdate("OS-1")).thenReturn(esperado);
        when(ordemRepository.findByCliente_IdOrderByDataAberturaDesc(2L)).thenReturn(List.of(ordem));
        when(ordemRepository.findAllByOrderByDataAberturaDesc()).thenReturn(List.of(ordem));
        when(ordemRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ordem)));

        assertSame(ordem, adapter.save(ordem));
        assertEquals(esperado, adapter.findById(1L));
        assertEquals(esperado, adapter.findByNumeroOs("OS-1"));
        assertEquals(esperado, adapter.findByNumeroOsForUpdate("OS-1"));
        assertEquals(List.of(ordem), adapter.findByClienteIdOrderByDataAberturaDesc(2L));
        assertEquals(List.of(ordem), adapter.findAllByOrderByDataAberturaDesc());
        assertEquals(List.of(ordem), adapter.findAll(
                new OrdemServicoFiltroInput(null, null, null), null, new PageQuery(0, 10)).content());

        var pageableCaptor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(ordemRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertTrue(pageable.getSort().isUnsorted());
    }

    @Test
    void metricsAdapterDeveOcultarProjectionsDosCasosDeUso() {
        var ordemProjection = mock(TempoMedioOrdemServicoProjection.class);
        var servicoProjection = mock(TempoMedioServicoProjection.class);
        when(ordemProjection.getQuantidadeOrdensFinalizadas()).thenReturn(4L);
        when(ordemProjection.getTempoMedioSegundos()).thenReturn(1800.0);
        when(servicoProjection.getServicoId()).thenReturn(7L);
        when(servicoProjection.getNomeServico()).thenReturn("Alinhamento");
        when(servicoProjection.getQuantidadeExecucoes()).thenReturn(3L);
        when(servicoProjection.getTempoMedioSegundos()).thenReturn(900.0);
        when(ordemRepository.calcularTempoMedioFinalizacao()).thenReturn(ordemProjection);
        when(servicoSolicitadoRepository.calcularTempoMedioPorServico())
                .thenReturn(List.of(servicoProjection));

        var adapter = new MetricsRepositoryAdapter(ordemRepository, servicoSolicitadoRepository);
        var ordem = adapter.calcularTempoMedioOrdensServico();
        var servicos = adapter.calcularTempoMedioPorServico();

        assertEquals(4L, ordem.quantidadeOrdensFinalizadas());
        assertEquals(1800.0, ordem.tempoMedioSegundos());
        assertEquals(7L, servicos.getFirst().servicoId());
        assertEquals("Alinhamento", servicos.getFirst().nomeServico());
        assertEquals(3L, servicos.getFirst().quantidadeExecucoes());
        assertEquals(900.0, servicos.getFirst().tempoMedioSegundos());
    }

    @Test
    void metricsAdapterDevePreservarTempoNuloQuandoProjectionDeOrdensForNula() {
        when(ordemRepository.calcularTempoMedioFinalizacao()).thenReturn(null);

        var ordem = new MetricsRepositoryAdapter(ordemRepository, servicoSolicitadoRepository)
                .calcularTempoMedioOrdensServico();

        assertEquals(0L, ordem.quantidadeOrdensFinalizadas());
        org.junit.jupiter.api.Assertions.assertNull(ordem.tempoMedioSegundos());
    }
}
