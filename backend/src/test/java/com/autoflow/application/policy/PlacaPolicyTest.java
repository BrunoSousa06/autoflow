package com.autoflow.application.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlacaPolicyTest {

    @Test
    void deveRemoverSeparadoresCaracteresInvalidosEConverterParaMaiusculas() {
        assertEquals("ABC1234", PlacaPolicy.normalizar("abc-12@34"));
    }
}
