package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import com.autoflow.infrastructure.persistence.repository.reparoadicional.ReparoAdicionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReparoAdicionalPersistenceAdapterTest {

    @Mock
    private ReparoAdicionalRepository repository;

    @InjectMocks
    private ReparoAdicionalPersistenceAdapter adapter;

    @Test
    void deveDelegarPersistenciaAoRepository() {
        var reparo = new ReparoAdicionalEntity();
        when(repository.save(reparo)).thenReturn(reparo);

        var resultado = adapter.save(reparo);

        assertSame(reparo, resultado);
        verify(repository).save(reparo);
    }

    @Test
    void deveDelegarBuscaPorIdAoRepository() {
        var reparo = new ReparoAdicionalEntity();
        var esperado = Optional.of(reparo);
        when(repository.findById(1L)).thenReturn(esperado);

        var resultado = adapter.findById(1L);

        assertEquals(esperado, resultado);
        verify(repository).findById(1L);
    }

    @Test
    void deveDelegarBuscaPorOrcamentoIdAoRepository() {
        var reparo = new ReparoAdicionalEntity();
        var esperado = Optional.of(reparo);
        when(repository.findByOrcamentoId(2L)).thenReturn(esperado);

        var resultado = adapter.findByOrcamentoId(2L);

        assertEquals(esperado, resultado);
        verify(repository).findByOrcamentoId(2L);
    }

    @Test
    void deveReceberRepositoryPorInjecaoDeDependencia() {
        var reparo = new ReparoAdicionalEntity();
        when(repository.save(reparo)).thenReturn(reparo);

        new ApplicationContextRunner()
                .withBean(ReparoAdicionalRepository.class, () -> repository)
                .withUserConfiguration(ReparoAdicionalPersistenceAdapter.class)
                .run(context -> {
                    var bean = context.getBean(ReparoAdicionalPersistenceAdapter.class);

                    assertSame(reparo, bean.save(reparo));
                });
    }
}
