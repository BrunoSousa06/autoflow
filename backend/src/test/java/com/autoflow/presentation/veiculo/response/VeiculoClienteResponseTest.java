package com.autoflow.presentation.veiculo.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoClienteResponseTest {

    @Test
    void deveCriarResponseComTodosOsCampos() {
        VeiculoClienteResponse response = new VeiculoClienteResponse(1L, "Toyota", 2020L, "ABC1234", "Corolla");

        assertEquals(1L, response.id());
        assertEquals("Toyota", response.marca());
        assertEquals(2020L, response.ano());
        assertEquals("ABC1234", response.placa());
        assertEquals("Corolla", response.modelo());
    }

    @Test
    void deveCriarResponseComCamposNulos() {
        VeiculoClienteResponse response = new VeiculoClienteResponse(null, null, null, null, null);

        assertNull(response.id());
        assertNull(response.marca());
        assertNull(response.ano());
        assertNull(response.placa());
        assertNull(response.modelo());
    }

    @Test
    void deveSerIgualQuandoMesmosValores() {
        VeiculoClienteResponse r1 = new VeiculoClienteResponse(1L, "Toyota", 2020L, "ABC1234", "Corolla");
        VeiculoClienteResponse r2 = new VeiculoClienteResponse(1L, "Toyota", 2020L, "ABC1234", "Corolla");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void deveSerDiferenteQuandoPlacasDiferentes() {
        VeiculoClienteResponse r1 = new VeiculoClienteResponse(1L, "Toyota", 2020L, "ABC1234", "Corolla");
        VeiculoClienteResponse r2 = new VeiculoClienteResponse(1L, "Toyota", 2020L, "XYZ9999", "Corolla");
        assertNotEquals(r1, r2);
    }

    @Test
    void deveGerarToStringComInformacoes() {
        VeiculoClienteResponse response = new VeiculoClienteResponse(1L, "Toyota", 2020L, "ABC1234", "Corolla");
        String str = response.toString();
        assertTrue(str.contains("Toyota"));
        assertTrue(str.contains("ABC1234"));
    }
}