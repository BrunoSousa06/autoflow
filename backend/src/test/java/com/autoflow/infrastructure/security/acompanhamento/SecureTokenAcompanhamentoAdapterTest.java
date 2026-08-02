package com.autoflow.infrastructure.security.acompanhamento;

import com.autoflow.infrastructure.security.SecureTokenAcompanhamentoAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureTokenAcompanhamentoAdapterTest {

    private SecureTokenAcompanhamentoAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SecureTokenAcompanhamentoAdapter();
    }

    @Test
    void deveGerarTokenComTamanhoEsperado() {
        var resultado = adapter.gerar();

        assertThat(resultado.token())
                .isNotBlank()
                .hasSize(43);

        assertThat(resultado.hash())
                .isNotBlank()
                .hasSize(64);
    }

    @Test
    void deveGerarHashSha256EmHexadecimal() {
        var resultado = adapter.gerar();

        assertThat(resultado.hash())
                .matches("[0-9a-f]{64}");
    }

    @Test
    void deveGerarTokensDiferentes() {
        var primeiro = adapter.gerar();
        var segundo = adapter.gerar();

        assertThat(primeiro.token())
                .isNotEqualTo(segundo.token());

        assertThat(primeiro.hash())
                .isNotEqualTo(segundo.hash());
    }

    @Test
    void deveCalcularMesmoHashParaMesmoToken() {
        String token = "token-publico-de-teste";

        String primeiroHash =
                adapter.calcularHash(token);

        String segundoHash =
                adapter.calcularHash(token);

        assertThat(primeiroHash)
                .isEqualTo(segundoHash)
                .hasSize(64);
    }

    @Test
    void hashNaoDeveSerIgualAoTokenOriginal() {
        String token = "token-publico-de-teste";

        String hash = adapter.calcularHash(token);

        assertThat(hash).isNotEqualTo(token);
    }
}