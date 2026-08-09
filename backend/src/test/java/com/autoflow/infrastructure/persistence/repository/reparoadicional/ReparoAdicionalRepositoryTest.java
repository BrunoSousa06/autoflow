package com.autoflow.infrastructure.persistence.repository.reparoadicional;

import com.autoflow.domain.ordemservico.reparoadicional.ReparoAdicionalEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReparoAdicionalRepositoryTest {

    @Test
    void deveDeclararBuscaPorOrcamentoId() throws NoSuchMethodException {
        Method method = ReparoAdicionalRepository.class.getMethod("findByOrcamentoId", Long.class);

        assertEquals(Optional.class, method.getReturnType());
        assertEquals("findByOrcamentoId", method.getName());
    }

    @Test
    void deveUsarReparoAdicionalComoEntidadeDoRepository() {
        Class<?> entidade = ReparoAdicionalRepository.class
                .getGenericInterfaces()[0]
                .getTypeName()
                .contains(ReparoAdicionalEntity.class.getName())
                ? ReparoAdicionalEntity.class
                : null;

        assertEquals(ReparoAdicionalEntity.class, entidade);
    }
}
