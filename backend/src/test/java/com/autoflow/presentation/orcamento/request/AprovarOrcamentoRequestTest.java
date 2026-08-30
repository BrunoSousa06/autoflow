package com.autoflow.presentation.orcamento.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AprovarOrcamentoRequestTest {

    @Test
    void deveCriarRequestComNome() {
        AprovarOrcamentoRequest request = new AprovarOrcamentoRequest("João Silva");
        assertEquals("João Silva", request.nome());
    }

    @Test
    void deveCriarRequestComNomeNulo() {
        AprovarOrcamentoRequest request = new AprovarOrcamentoRequest(null);
        assertNull(request.nome());
    }

    @Test
    void deveSerIgualQuandoMesmosValores() {
        AprovarOrcamentoRequest r1 = new AprovarOrcamentoRequest("João");
        AprovarOrcamentoRequest r2 = new AprovarOrcamentoRequest("João");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void deveSerDiferenteQuandoValoresDiferentes() {
        AprovarOrcamentoRequest r1 = new AprovarOrcamentoRequest("João");
        AprovarOrcamentoRequest r2 = new AprovarOrcamentoRequest("Maria");
        assertNotEquals(r1, r2);
    }

    @Test
    void deveGerarToStringCorreto() {
        AprovarOrcamentoRequest request = new AprovarOrcamentoRequest("João");
        assertTrue(request.toString().contains("João"));
    }
}
