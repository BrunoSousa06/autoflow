package com.autoflow.domain.ordemservico.acompanhamento;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AcessoAcompanhamentoTest {

    private static final LocalDateTime CRIADO_EM =
            LocalDateTime.of(2026, Month.AUGUST, 1, 10, 0);

    @Test
    void deveEstarDisponivelQuandoTokenNaoExpirou() {
        AcessoAcompanhamento acesso =
                new AcessoAcompanhamento(
                        "token-hash",
                        CRIADO_EM,
                        CRIADO_EM.plusDays(30),
                        null
                );

        boolean disponivel = acesso.estaDisponivelEm(
                CRIADO_EM.plusDays(10)
        );

        assertThat(disponivel).isTrue();
    }

    @Test
    void deveEstarDisponivelQuandoNaoPossuiExpiracao() {
        AcessoAcompanhamento acesso =
                new AcessoAcompanhamento(
                        "token-hash",
                        CRIADO_EM,
                        null,
                        null
                );

        boolean disponivel = acesso.estaDisponivelEm(
                CRIADO_EM.plusYears(1)
        );

        assertThat(disponivel).isTrue();
    }

    @Test
    void naoDeveEstarDisponivelQuandoExpirado() {
        AcessoAcompanhamento acesso =
                new AcessoAcompanhamento(
                        "token-hash",
                        CRIADO_EM,
                        CRIADO_EM.plusDays(30),
                        null
                );

        boolean disponivel = acesso.estaDisponivelEm(
                CRIADO_EM.plusDays(31)
        );

        assertThat(disponivel).isFalse();
    }

    @Test
    void naoDeveEstarDisponivelNoInstanteExatoDaExpiracao() {
        LocalDateTime expiraEm = CRIADO_EM.plusDays(30);

        AcessoAcompanhamento acesso =
                new AcessoAcompanhamento(
                        "token-hash",
                        CRIADO_EM,
                        expiraEm,
                        null
                );

        boolean disponivel =
                acesso.estaDisponivelEm(expiraEm);

        assertThat(disponivel).isFalse();
    }

    @Test
    void naoDeveEstarDisponivelQuandoRevogado() {
        AcessoAcompanhamento acesso =
                new AcessoAcompanhamento(
                        "token-hash",
                        CRIADO_EM,
                        CRIADO_EM.plusDays(30),
                        CRIADO_EM.plusDays(1)
                );

        boolean disponivel = acesso.estaDisponivelEm(
                CRIADO_EM.plusDays(2)
        );

        assertThat(disponivel).isFalse();
    }

    @Test
    void deveRejeitarHashNulo() {
        LocalDateTime expiraEm = CRIADO_EM.plusDays(30);
        assertThatThrownBy(
                () -> new AcessoAcompanhamento(
                        null,
                        CRIADO_EM,
                        expiraEm,
                        null
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Hash do token é obrigatório");
    }

    @Test
    void deveRejeitarHashVazio() {
        LocalDateTime expiraEm = CRIADO_EM.plusDays(30);
        assertThatThrownBy(
                () -> new AcessoAcompanhamento(
                        " ",
                        CRIADO_EM,
                        expiraEm,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Hash do token não pode estar vazio");
    }

    @Test
    void deveRejeitarExpiracaoAnteriorACriacao() {
        LocalDateTime expiraEm = CRIADO_EM.minusMinutes(1);
        assertThatThrownBy(
                () -> new AcessoAcompanhamento(
                        "token-hash",
                        CRIADO_EM,
                        expiraEm,
                        null
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expiração deve ser posterior à criação");
    }
}
