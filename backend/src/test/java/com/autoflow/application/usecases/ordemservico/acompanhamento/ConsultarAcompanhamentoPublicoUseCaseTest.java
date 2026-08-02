package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.exception.AcompanhamentoPublicoNaoEncontradoException;
import com.autoflow.application.exception.TokenAcompanhamentoObrigatorioException;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.ordemservico.acompanhamento.AcessoAcompanhamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarAcompanhamentoPublicoUseCaseTest {

    private static final String TOKEN = "token-publico";
    private static final String TOKEN_HASH = "token-hash";

    private static final Instant INSTANTE_ATUAL =
            Instant.parse("2026-08-01T12:00:00Z");

    @Mock
    private AcompanhamentoPublicoGateway acompanhamentoGateway;

    @Mock
    private TokenAcompanhamentoGateway tokenGateway;

    private ConsultarAcompanhamentoPublicoUseCase useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                INSTANTE_ATUAL,
                ZoneOffset.UTC
        );

        useCase = new ConsultarAcompanhamentoPublicoUseCase(
                acompanhamentoGateway,
                tokenGateway,
                clock
        );
    }

    @Test
    void deveConsultarAcompanhamentoComTokenValido() {
        LocalDateTime agora = agora();

        var acesso = new AcessoAcompanhamento(
                TOKEN_HASH,
                agora.minusDays(1),
                agora.plusDays(29),
                null
        );

        var dados =
                new AcompanhamentoPublicoGateway
                        .DadosAcompanhamentoPublico(
                        "OS-2026-000001",
                        StatusOrdemServico.RECEBIDA,
                        agora.minusDays(1),
                        null,
                        null,
                        null,
                        null,
                        acesso
                );

        when(tokenGateway.calcularHash(TOKEN))
                .thenReturn(TOKEN_HASH);

        when(acompanhamentoGateway.buscarPorTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(dados));

        var resultado = useCase.execute(TOKEN);

        assertThat(resultado.numeroOs())
                .isEqualTo("OS-2026-000001");

        assertThat(resultado.status())
                .isEqualTo(StatusOrdemServico.RECEBIDA);

        assertThat(resultado.dataAbertura())
                .isEqualTo(agora.minusDays(1));

        verify(tokenGateway).calcularHash(TOKEN);
        verify(acompanhamentoGateway)
                .buscarPorTokenHash(TOKEN_HASH);
    }

    @Test
    void deveRejeitarTokenNulo() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(
                        TokenAcompanhamentoObrigatorioException.class
                );

        verifyNoInteractions(
                tokenGateway,
                acompanhamentoGateway
        );
    }

    @Test
    void deveRejeitarTokenVazio() {
        assertThatThrownBy(() -> useCase.execute(" "))
                .isInstanceOf(
                        TokenAcompanhamentoObrigatorioException.class
                );

        verifyNoInteractions(
                tokenGateway,
                acompanhamentoGateway
        );
    }

    @Test
    void deveRetornarNaoEncontradoQuandoHashNaoExiste() {
        when(tokenGateway.calcularHash(TOKEN))
                .thenReturn(TOKEN_HASH);

        when(acompanhamentoGateway.buscarPorTokenHash(TOKEN_HASH))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(TOKEN))
                .isInstanceOf(
                        AcompanhamentoPublicoNaoEncontradoException.class
                )
                .hasMessage(
                        "Acompanhamento público não encontrado"
                );
    }

    @Test
    void deveRetornarNaoEncontradoQuandoTokenExpirou() {
        LocalDateTime agora = agora();

        var acesso = new AcessoAcompanhamento(
                TOKEN_HASH,
                agora.minusDays(31),
                agora.minusDays(1),
                null
        );

        var dados = criarDados(acesso);

        when(tokenGateway.calcularHash(TOKEN))
                .thenReturn(TOKEN_HASH);

        when(acompanhamentoGateway.buscarPorTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(dados));

        assertThatThrownBy(() -> useCase.execute(TOKEN))
                .isInstanceOf(
                        AcompanhamentoPublicoNaoEncontradoException.class
                );
    }

    @Test
    void deveRetornarNaoEncontradoQuandoTokenFoiRevogado() {
        LocalDateTime agora = agora();

        var acesso = new AcessoAcompanhamento(
                TOKEN_HASH,
                agora.minusDays(1),
                agora.plusDays(29),
                agora.minusHours(1)
        );

        var dados = criarDados(acesso);

        when(tokenGateway.calcularHash(TOKEN))
                .thenReturn(TOKEN_HASH);

        when(acompanhamentoGateway.buscarPorTokenHash(TOKEN_HASH))
                .thenReturn(Optional.of(dados));

        assertThatThrownBy(() -> useCase.execute(TOKEN))
                .isInstanceOf(
                        AcompanhamentoPublicoNaoEncontradoException.class
                );
    }

    private AcompanhamentoPublicoGateway
            .DadosAcompanhamentoPublico criarDados(
            AcessoAcompanhamento acesso
    ) {
        return new AcompanhamentoPublicoGateway
                .DadosAcompanhamentoPublico(
                "OS-2026-000001",
                StatusOrdemServico.RECEBIDA,
                agora().minusDays(1),
                null,
                null,
                null,
                null,
                acesso
        );
    }

    private LocalDateTime agora() {
        return LocalDateTime.ofInstant(
                INSTANTE_ATUAL,
                ZoneOffset.UTC
        );
    }
}
