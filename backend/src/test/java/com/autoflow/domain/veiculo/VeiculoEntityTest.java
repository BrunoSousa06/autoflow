package com.autoflow.domain.veiculo;

import com.autoflow.domain.cliente.ClienteEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VeiculoEntityTest {

    private VeiculoEntity veiculo;
    private ClienteEntity cliente;

    @BeforeEach
    void setUp() {
        cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("joao@example.com");
        cliente.setTelefone("11999999999");

        veiculo = new VeiculoEntity();
        veiculo.setId(1L);
        veiculo.setCliente(cliente);
        veiculo.setMarca("Toyota");
        veiculo.setModelo("Corolla");
        veiculo.setAno(2022);
        veiculo.setPlaca("ABC1234");
    }

    @Test
    void testVeiculoCreation() {
        assertNotNull(veiculo);
        assertEquals(1L, veiculo.getId());
        assertEquals("Toyota", veiculo.getMarca());
        assertEquals("Corolla", veiculo.getModelo());
        assertEquals(2022, veiculo.getAno());
        assertEquals("ABC1234", veiculo.getPlaca());
    }

    @Test
    void testVeiculoClienteAssociation() {
        assertNotNull(veiculo.getCliente());
        assertEquals("João Silva", veiculo.getCliente().getNome());
        assertEquals("12345678901", veiculo.getCliente().getCpfCnpj());
    }

    @Test
    void testVeiculoSetters() {
        veiculo.setMarca("Honda");
        veiculo.setModelo("Civic");
        veiculo.setAno(2023);
        veiculo.setPlaca("XYZ9876");

        assertEquals("Honda", veiculo.getMarca());
        assertEquals("Civic", veiculo.getModelo());
        assertEquals(2023, veiculo.getAno());
        assertEquals("XYZ9876", veiculo.getPlaca());
    }

    @Test
    void testVeiculoWithoutCliente() {
        VeiculoEntity veiculoSemCliente = new VeiculoEntity();
        veiculoSemCliente.setMarca("Ford");
        veiculoSemCliente.setModelo("Ka");

        assertNull(veiculoSemCliente.getCliente());
        assertEquals("Ford", veiculoSemCliente.getMarca());
    }

    @Test
    void testVeiculoPlacaUniqueness() {
        VeiculoEntity veiculo2 = new VeiculoEntity();
        veiculo2.setPlaca("ABC1234");
        veiculo2.setMarca("Honda");

        assertEquals(veiculo.getPlaca(), veiculo2.getPlaca());
    }
}
