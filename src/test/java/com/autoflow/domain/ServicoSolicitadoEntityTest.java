package com.autoflow.domain;

import com.autoflow.domain.ordemServico.ServicoSolicitadoEntity;
import org.junit.jupiter.api.Test;



import static org.junit.jupiter.api.Assertions.*;

class ServicoSolicitadoEntityTest {

    @Test
    void deveCriarServicoSolicitado() {
        Long servicoId = 1L;

        ServicoSolicitadoEntity servico = new ServicoSolicitadoEntity(servicoId, "Alinhamento");

        assertEquals(servicoId, servico.getServicoId());
        assertEquals("Alinhamento", servico.getNome());
    }

    @Test
    void deveValidarCamposObrigatorios() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitadoEntity(null, "Alinhamento")),
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitadoEntity(1L, null)),
                () -> assertThrows(IllegalArgumentException.class, () -> new ServicoSolicitadoEntity(1L, " "))
        );
    }
}
