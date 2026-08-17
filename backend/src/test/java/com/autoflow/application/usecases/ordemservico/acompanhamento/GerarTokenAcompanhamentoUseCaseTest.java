package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.TokenAcompanhamentoOutput;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.application.usecases.ordemservico.acompanhamento.GerarTokenAcompanhamentoUseCaseImpl;
import com.autoflow.domain.ordemservico.acompanhamento.AcessoAcompanhamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GerarTokenAcompanhamentoUseCaseTest {

    private static final Long ORDEM_SERVICO_ID = 10L;
    private static final String TOKEN = "token-original";
    private static final String TOKEN_HASH = "token-hash";

    private static final Instant INSTANTE_ATUAL =
            Instant.parse("2026-08-01T12:00:00Z");

    @Mock
    private TokenAcompanhamentoGateway tokenGateway;

    @Mock
    private AcompanhamentoPublicoGateway acompanhamentoGateway;

    private GerarTokenAcompanhamentoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                INSTANTE_ATUAL,
                ZoneOffset.UTC
        );

        useCase = new GerarTokenAcompanhamentoUseCaseImpl(
                tokenGateway,
                acompanhamentoGateway,
                clock
        );
    }

    @Test
    void deveGerarESalvarTokenDeAcompanhamento() {
        TokenAcompanhamentoOutput tokenGerado =
                new TokenAcompanhamentoOutput(
                        TOKEN,
                        TOKEN_HASH
                );

        when(tokenGateway.gerar())
                .thenReturn(tokenGerado);

        TokenAcompanhamentoOutput resultado =
                useCase.execute(ORDEM_SERVICO_ID);

        ArgumentCaptor<AcessoAcompanhamento> captor =
                ArgumentCaptor.forClass(
                        AcessoAcompanhamento.class
                );

        verify(acompanhamentoGateway).salvar(
                eq(ORDEM_SERVICO_ID),
                captor.capture()
        );

        AcessoAcompanhamento acessoSalvo =
                captor.getValue();

        LocalDateTime criadoEm =
                LocalDateTime.ofInstant(
                        INSTANTE_ATUAL,
                        ZoneOffset.UTC
                );

        assertThat(resultado).isEqualTo(tokenGerado);
        assertThat(acessoSalvo.tokenHash())
                .isEqualTo(TOKEN_HASH);
        assertThat(acessoSalvo.criadoEm())
                .isEqualTo(criadoEm);
        assertThat(acessoSalvo.expiraEm())
                .isEqualTo(criadoEm.plusDays(30));
        assertThat(acessoSalvo.revogadoEm())
                .isNull();

        verify(tokenGateway).gerar();
    }

    @Test
    void deveRejeitarIdNulo() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "ID da ordem de serviço é obrigatório"
                );

        verifyNoInteractions(
                tokenGateway,
                acompanhamentoGateway
        );
    }

    @Test
    void deveRejeitarIdZero() {
        assertThatThrownBy(() -> useCase.execute(0L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "ID da ordem de serviço deve ser positivo"
                );

        verifyNoInteractions(
                tokenGateway,
                acompanhamentoGateway
        );
    }

    @Test
    void deveRejeitarIdNegativo() {
        assertThatThrownBy(() -> useCase.execute(-1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "ID da ordem de serviço deve ser positivo"
                );

        verifyNoInteractions(
                tokenGateway,
                acompanhamentoGateway
        );
    }
}
