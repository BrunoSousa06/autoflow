package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.orcamento.TipoOrcamento;
import com.autoflow.domain.ordemservico.HistoricoStatusOsEntity;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import com.autoflow.domain.pecainsumo.PecaInsumoEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.infrastructure.persistence.repository.PecaInsumoRepository;
import com.autoflow.infrastructure.persistence.repository.UsuarioRepository;
import com.autoflow.repository.orcamento.OrcamentoRepository;
import com.autoflow.repository.ordemservico.OrdemServicoRepository;
import com.autoflow.repository.ordemservico.TempoMedioOrdemServicoProjection;
import com.autoflow.repository.ordemservico.historico.HistoricoStatusOsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

    @Test
    void pecaAdapterDeveDelegarTodasOperacoes() {
        var adapter = new PecaInsumoAdapter(pecaRepository);
        var entity = new PecaInsumoEntity();
        entity.setId(1L);
        entity.setTipo(CategoriaPecaInsumo.PECA);
        var pageable = PageRequest.of(0, 10);
        Specification<PecaInsumoEntity> spec = (root, query, cb) -> cb.conjunction();

        when(pecaRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(pecaRepository.findByNomeIgnoreCase("Filtro")).thenReturn(Optional.of(entity));
        when(pecaRepository.save(entity)).thenReturn(entity);
        when(pecaRepository.findAll()).thenReturn(List.of(entity));
        when(pecaRepository.findAll(spec, pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(pecaRepository.existsById(1L)).thenReturn(true);
        when(pecaRepository.findAllById(List.of(1L))).thenReturn(List.of(entity));

        assertEquals(Optional.of(entity), adapter.findById(1L));
        assertEquals(Optional.of(entity), adapter.findByNomeIgnoreCase("Filtro"));
        assertSame(entity, adapter.save(entity));
        assertEquals(List.of(entity), adapter.findAll());
        assertEquals(List.of(entity), adapter.findAll(spec, pageable).getContent());
        assertTrue(adapter.existsById(1L));
        assertEquals(List.of(entity), adapter.findAllById(List.of(1L)));
        adapter.deleteById(1L);
        adapter.saveAll(List.of(entity));

        verify(pecaRepository).deleteById(1L);
        verify(pecaRepository).saveAll(List.of(entity));
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
        var pageable = PageRequest.of(0, 10);
        Specification<OrdemServicoEntity> spec = (root, query, cb) -> cb.conjunction();
        var projection = mock(TempoMedioOrdemServicoProjection.class);
        when(ordemRepository.save(ordem)).thenReturn(ordem);
        when(ordemRepository.findById(1L)).thenReturn(esperado);
        when(ordemRepository.findByNumeroOs("OS-1")).thenReturn(esperado);
        when(ordemRepository.findByCliente_IdOrderByDataAberturaDesc(2L)).thenReturn(List.of(ordem));
        when(ordemRepository.findAllByOrderByDataAberturaDesc()).thenReturn(List.of(ordem));
        when(ordemRepository.findAll(spec, pageable)).thenReturn(new PageImpl<>(List.of(ordem)));
        when(ordemRepository.calcularTempoMedioFinalizacao()).thenReturn(projection);

        assertSame(ordem, adapter.save(ordem));
        assertEquals(esperado, adapter.findById(1L));
        assertEquals(esperado, adapter.findByNumeroOs("OS-1"));
        assertEquals(List.of(ordem), adapter.findByClienteIdOrderByDataAberturaDesc(2L));
        assertEquals(List.of(ordem), adapter.findAllByOrderByDataAberturaDesc());
        assertEquals(List.of(ordem), adapter.findAll(spec, pageable).getContent());
        assertSame(projection, adapter.calcularTempoMedioFinalizacao());
    }
}
