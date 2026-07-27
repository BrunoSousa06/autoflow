package com.autoflow.presentation.cliente.response;

import com.autoflow.presentation.cliente.response.ClienteVeiculoResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClienteVeiculoResponseTest {

    @Test
    void deveCriarResponseComTodosOsCampos() {
        ClienteVeiculoResponse response = new ClienteVeiculoResponse(
                1L, "João Silva", "12345678901", "11999999999", "joao@email.com"
        );

        assertEquals(1L, response.id());
        assertEquals("João Silva", response.nome());
        assertEquals("12345678901", response.cpfCnpj());
        assertEquals("11999999999", response.telefone());
        assertEquals("joao@email.com", response.email());
    }

    @Test
    void deveCriarResponseComCamposNulos() {
        ClienteVeiculoResponse response = new ClienteVeiculoResponse(null, null, null, null, null);

        assertNull(response.id());
        assertNull(response.nome());
        assertNull(response.cpfCnpj());
        assertNull(response.telefone());
        assertNull(response.email());
    }

    @Test
    void deveSerIgualQuandoMesmosValores() {
        ClienteVeiculoResponse r1 = new ClienteVeiculoResponse(1L, "João", "12345678901", "11999999999", "joao@email.com");
        ClienteVeiculoResponse r2 = new ClienteVeiculoResponse(1L, "João", "12345678901", "11999999999", "joao@email.com");
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void deveSerDiferenteQuandoIdsDiferentes() {
        ClienteVeiculoResponse r1 = new ClienteVeiculoResponse(1L, "João", "12345678901", "11999999999", "joao@email.com");
        ClienteVeiculoResponse r2 = new ClienteVeiculoResponse(2L, "João", "12345678901", "11999999999", "joao@email.com");
        assertNotEquals(r1, r2);
    }
}