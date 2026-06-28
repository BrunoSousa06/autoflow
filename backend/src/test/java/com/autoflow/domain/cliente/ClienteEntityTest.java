package com.autoflow.domain.cliente;

import com.autoflow.domain.usuario.UsuarioEntity;
import com.autoflow.domain.usuario.RoleEnum;
import com.autoflow.domain.veiculo.VeiculoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClienteEntityTest {

    private ClienteEntity cliente;
    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioEntity();
        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setRole(RoleEnum.CLIENTE);

        cliente = new ClienteEntity();
        cliente.setId(1L);
        cliente.setNome("João Silva");
        cliente.setCpfCnpj("12345678901");
        cliente.setEmail("joao@example.com");
        cliente.setTelefone("11999999999");
        cliente.setUsuario(usuario);
        cliente.setVeiculos(new ArrayList<>());
    }

    @Test
    void testClienteCreation() {
        assertNotNull(cliente);
        assertEquals(1L, cliente.getId());
        assertEquals("João Silva", cliente.getNome());
        assertEquals("12345678901", cliente.getCpfCnpj());
        assertEquals("joao@example.com", cliente.getEmail());
        assertEquals("11999999999", cliente.getTelefone());
    }

    @Test
    void testClienteUsuarioAssociation() {
        assertNotNull(cliente.getUsuario());
        assertEquals("João Silva", cliente.getUsuario().getNome());
        assertEquals(RoleEnum.CLIENTE, cliente.getUsuario().getRole());
    }

    @Test
    void testClienteVeiculosAssociation() {
        assertNotNull(cliente.getVeiculos());
        assertTrue(cliente.getVeiculos().isEmpty());

        VeiculoEntity veiculo = new VeiculoEntity();
        veiculo.setId(1L);
        veiculo.setMarca("Toyota");
        veiculo.setPlaca("ABC1234");
        veiculo.setCliente(cliente);

        cliente.getVeiculos().add(veiculo);

        assertEquals(1, cliente.getVeiculos().size());
        assertEquals("Toyota", cliente.getVeiculos().get(0).getMarca());
    }

    @Test
    void testClienteSetters() {
        cliente.setNome("Maria Silva");
        cliente.setCpfCnpj("98765432101");
        cliente.setEmail("maria@example.com");
        cliente.setTelefone("11888888888");

        assertEquals("Maria Silva", cliente.getNome());
        assertEquals("98765432101", cliente.getCpfCnpj());
        assertEquals("maria@example.com", cliente.getEmail());
        assertEquals("11888888888", cliente.getTelefone());
    }

    @Test
    void testClienteEmailUniqueness() {
        ClienteEntity cliente2 = new ClienteEntity();
        cliente2.setEmail("joao@example.com");

        assertEquals(cliente.getEmail(), cliente2.getEmail());
    }

    @Test
    void testClienteCpfCnpjUniqueness() {
        ClienteEntity cliente2 = new ClienteEntity();
        cliente2.setCpfCnpj("12345678901");

        assertEquals(cliente.getCpfCnpj(), cliente2.getCpfCnpj());
    }
}
