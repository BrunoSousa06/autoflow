package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.input.servico.PageInput;
import com.autoflow.domain.servico.Servico;
import com.autoflow.infrastructure.persistence.entity.servico.ServicoEntity;
import com.autoflow.infrastructure.persistence.mapper.ServicoPersistenceMapper;
import com.autoflow.infrastructure.persistence.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoRepositoryAdapterTest {

    @Mock
    private ServicoRepository repository;

    @Mock
    private ServicoPersistenceMapper mapper;

    @Test
    void deveMapearDominioAoSalvarEMapearRetornoParaDominio() {
        Servico domain = servico(1L, true);
        ServicoEntity entity = new ServicoEntity();
        when(mapper.mapToEntity(domain)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.mapToDomain(entity)).thenReturn(domain);

        Servico result = adapter().save(domain);

        assertEquals(domain, result);
        verify(mapper).mapToEntity(domain);
        verify(mapper).mapToDomain(entity);
    }

    @Test
    void deveAtualizarEntidadeEncontradaEMapearRetorno() {
        Servico domain = servico(1L, true);
        ServicoEntity entity = new ServicoEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.mapToDomain(entity)).thenReturn(domain);

        Servico result = adapter().update(domain);

        assertEquals(domain, result);
        verify(mapper).updateEntity(domain, entity);
        verify(repository).save(entity);
    }

    @Test
    void deveConsultarPorIdEPelaExistenciaDoNome() {
        Servico domain = servico(1L, true);
        ServicoEntity entity = new ServicoEntity();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.findByNomeIgnoreCase("Alinhamento")).thenReturn(Optional.of(entity));
        when(mapper.mapToDomain(entity)).thenReturn(domain);

        assertEquals(Optional.of(domain), adapter().findById(1L));
        assertEquals(true, adapter().existsByNomeIgnoreCase("Alinhamento"));
        verify(mapper).mapToDomain(entity);
    }

    @Test
    void deveInativarRegistroExistenteSemRemoveLo() {
        ServicoEntity entity = new ServicoEntity();
        entity.setAtivo(true);
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        adapter().inativar(1L);

        assertEquals(false, entity.isAtivo());
        verify(repository).save(entity);
    }

    @Test
    void deveConverterPaginaComOrdenacaoPorIdDecrescente() {
        Servico domain = servico(1L, true);
        ServicoEntity entity = new ServicoEntity();
        when(repository.findAllByAtivoTrue(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(1, 10, Sort.by("id").descending()), 11));
        when(mapper.mapToDomain(entity)).thenReturn(domain);

        var result = adapter().findAllByAtivoTrue(new PageInput(1, 10));

        assertEquals(List.of(domain), result.content());
        assertEquals(1, result.page());
        assertEquals(10, result.size());
        assertEquals(11, result.totalElements());
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllByAtivoTrue(pageable.capture());
        assertEquals(1, pageable.getValue().getPageNumber());
        assertEquals(10, pageable.getValue().getPageSize());
        assertEquals("id: DESC", pageable.getValue().getSort().toString());
    }

    private ServicoRepositoryAdapter adapter() {
        return new ServicoRepositoryAdapter(repository, mapper);
    }

    private static Servico servico(Long id, boolean ativo) {
        return Servico.reconstituir(id, "Alinhamento", "Rodas", new BigDecimal("99.90"), ativo);
    }
}
