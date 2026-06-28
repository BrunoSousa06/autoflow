package com.autoflow.controller.ordemservico.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarLaudoRequestTest {

    @Test
    void deveCriarRequestComLaudo() {
        RegistrarLaudoRequest request = new RegistrarLaudoRequest("Motor com desgaste excessivo");
        assertEquals("Motor com desgaste excessivo", request.laudo());
    }

    @Test
    void deveCriarRequestComLaudoNulo() {
        RegistrarLaudoRequest request = new RegistrarLaudoRequest(null);
        assertNull(request.laudo());
    }

    @Test
    void deveSerIgualQuandoMesmosValores() {
        RegistrarLaudoRequest r1 = new RegistrarLaudoRequest("laudo");
        RegistrarLaudoRequest r2 = new RegistrarLaudoRequest("laudo");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void deveSerDiferenteQuandoLaudosDiferentes() {
        RegistrarLaudoRequest r1 = new RegistrarLaudoRequest("laudo A");
        RegistrarLaudoRequest r2 = new RegistrarLaudoRequest("laudo B");
        assertNotEquals(r1, r2);
    }

    @Test
    void deveGerarToStringComLaudo() {
        RegistrarLaudoRequest request = new RegistrarLaudoRequest("laudo teste");
        assertTrue(request.toString().contains("laudo teste"));
    }
}
