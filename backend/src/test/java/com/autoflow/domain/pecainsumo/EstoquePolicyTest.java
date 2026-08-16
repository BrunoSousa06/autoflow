package com.autoflow.domain.pecainsumo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstoquePolicyTest {

    @ParameterizedTest
    @CsvSource({
            "5, 1, true",
            "5, 5, true",
            "5, 6, false"
    })
    void deveClassificarDisponibilidade(int disponivel, int necessario, boolean esperado) {
        var resultado = EstoquePolicy.classificar(disponivel, necessario);

        assertEquals(esperado, resultado.disponivel());
        assertEquals(disponivel, resultado.quantidadeDisponivel());
    }

    @Test
    void deveRejeitarQuantidadeNecessariaNaoPositiva() {
        assertThrows(IllegalArgumentException.class,
                () -> EstoquePolicy.classificar(5, 0));
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        assertThrows(IllegalStateException.class,
                () -> EstoquePolicy.classificar(-1, 1));
    }

    @Test
    void deveCalcularQuantidadeRestanteAposBaixa() {
        assertEquals(40, EstoquePolicy.calcularQuantidadeRestante(50, 10));
    }

    @Test
    void deveRejeitarBaixaMaiorQueEstoque() {
        assertThrows(IllegalStateException.class,
                () -> EstoquePolicy.calcularQuantidadeRestante(5, 6));
    }
}
