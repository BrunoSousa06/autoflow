package com.autoflow.domain.servico;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicoTest {

    @Test
    void deveCriarServicoAtivoSemDependenciaDePersistencia() {
        Servico servico = Servico.criar("Alinhamento", "Geometria das rodas", new BigDecimal("99.90"));

        assertEquals(true, servico.ativo());
        assertEquals("Alinhamento", servico.nome());
    }

    @Test
    void deveRejeitarDadosInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> Servico.criar("", "Descricao", BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> Servico.criar("Servico", "", BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class,
                () -> Servico.criar("Servico", "Descricao", BigDecimal.valueOf(-1)));
    }

    @Test
    void deveAtualizarDadosSemAlterarIdOuStatus() {
        Servico original = Servico.reconstituir(7L, "Alinhamento", "Rodas", BigDecimal.TEN, false);

        Servico atualizado = original.atualizar("Balanceamento", "Pneus", BigDecimal.ONE);

        assertEquals(7L, atualizado.id());
        assertEquals(false, atualizado.ativo());
        assertEquals("Balanceamento", atualizado.nome());
    }
}
