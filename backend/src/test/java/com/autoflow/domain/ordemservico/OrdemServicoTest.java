package com.autoflow.domain.ordemservico;

import com.autoflow.domain.veiculo.Veiculo;

import com.autoflow.domain.cliente.Cliente;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {

    @Test
    void deveCriarEAvancarUmaOrdemServico() {
        Cliente cliente = Cliente.reconstituir(1L, "Cliente", "123", "11999999999", "cliente@email.com");
        OrdemServico ordem = OrdemServico.criar(cliente, new Veiculo(2L, "ABC1D23", "Honda", "Civic", 2020));
        ordem.adicionarServicosSolicitados(List.of(ServicoSolicitado.criar(10L, "Revisao", BigDecimal.TEN)));

        ordem.iniciarDiagnostico();
        ordem.registrarLaudo("Laudo");
        ordem.finalizarDiagnostico();
        ordem.aguardarAprovacao();
        ordem.iniciarExecucao();

        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
        assertEquals(1L, ordem.getClienteId());
        assertEquals("ABC1D23", ordem.getVeiculoPlaca());
    }

    @Test
    void deveRecusarTransicaoInvalida() {
        OrdemServico ordem = OrdemServico.criar(
                Cliente.reconstituir(1L, "Cliente", "123", null, "cliente@email.com"),
                new Veiculo(2L, "ABC1D23", null, null, null));

        assertThrows(IllegalStateException.class, ordem::entregar);
    }
}
