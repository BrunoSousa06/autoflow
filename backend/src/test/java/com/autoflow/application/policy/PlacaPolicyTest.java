package com.autoflow.application.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlacaPolicyTest {

    @Test
    void deveRemoverSeparadoresCaracteresInvalidosEConverterParaMaiusculas() {
        assertEquals("ABC1234", PlacaPolicy.normalizar("abc-12@34"));
    }

    @Test
    void deveRetornarNuloQuandoPlacaForNula() {
        assertNull(PlacaPolicy.normalizar(null));
    }
}
