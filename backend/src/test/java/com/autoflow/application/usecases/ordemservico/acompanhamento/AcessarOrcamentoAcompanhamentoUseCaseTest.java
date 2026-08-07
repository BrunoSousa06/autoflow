package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.exception.AcompanhamentoPublicoNaoEncontradoException;
import com.autoflow.application.exception.TokenAcompanhamentoObrigatorioException;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.application.usecases.orcamento.ConsultarOrcamentoDaOsUseCase;
import com.autoflow.application.usecases.orcamento.DecidirOrcamentoUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.domain.ordemservico.StatusOrdemServico;
import com.autoflow.domain.ordemservico.acompanhamento.AcessoAcompanhamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcessarOrcamentoAcompanhamentoUseCaseTest {

    private static final String TOKEN = "token-publico";
    private static final String TOKEN_HASH = "token-hash";
    private static final String NUMERO_OS = "OS-2026-000001";
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, Month.AUGUST, 1, 12, 0);

    @Mock private AcompanhamentoPublicoGateway acompanhamentoGateway;
    @Mock private TokenAcompanhamentoGateway tokenGateway;
    @Mock private ConsultarOrcamentoDaOsUseCase consultarOrcamentoDaOsUseCase;
    @Mock private DecidirOrcamentoUseCase decidirOrcamentoUseCase;
    @Mock private OrcamentoEntity orcamento;

    private AcessarOrcamentoAcompanhamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        var clock = Clock.fixed(Instant.parse("2026-08-01T12:00:00Z"), ZoneOffset.UTC);
        useCase = new AcessarOrcamentoAcompanhamentoUseCase(
                acompanhamentoGateway, tokenGateway, consultarOrcamentoDaOsUseCase, decidirOrcamentoUseCase, clock);
    }

    @Test
    void deveConsultarOrcamentoQuandoTokenEstiverDisponivel() {
        prepararAcessoDisponivel();
        when(consultarOrcamentoDaOsUseCase.execute(10L, NUMERO_OS)).thenReturn(orcamento);

        assertThat(useCase.consultar(10L, TOKEN)).isSameAs(orcamento);

        verify(consultarOrcamentoDaOsUseCase).execute(10L, NUMERO_OS);
    }

    @Test
    void deveAprovarOrcamentoQuandoTokenEstiverDisponivel() {
        prepararAcessoDisponivel();
        when(decidirOrcamentoUseCase.aprovarDaOrdem(10L, NUMERO_OS)).thenReturn(orcamento);

        assertThat(useCase.aprovar(10L, TOKEN)).isSameAs(orcamento);

        verify(decidirOrcamentoUseCase).aprovarDaOrdem(10L, NUMERO_OS);
    }

    @Test
    void deveRejeitarTokenNuloOuEmBranco() {
        assertThatThrownBy(() -> useCase.consultar(10L, null))
                .isInstanceOf(TokenAcompanhamentoObrigatorioException.class);
        assertThatThrownBy(() -> useCase.consultar(10L, " "))
                .isInstanceOf(TokenAcompanhamentoObrigatorioException.class);

        verifyNoInteractions(tokenGateway, acompanhamentoGateway, consultarOrcamentoDaOsUseCase, decidirOrcamentoUseCase);
    }

    @Test
    void deveRejeitarTokenDesconhecido() {
        when(tokenGateway.calcularHash(TOKEN)).thenReturn(TOKEN_HASH);
        when(acompanhamentoGateway.buscarPorTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.consultar(10L, TOKEN))
                .isInstanceOf(AcompanhamentoPublicoNaoEncontradoException.class);

        verifyNoInteractions(consultarOrcamentoDaOsUseCase, decidirOrcamentoUseCase);
    }

    @Test
    void deveRejeitarAcessoExpirado() {
        var acesso = new AcessoAcompanhamento(
                TOKEN_HASH, AGORA.minusDays(2), AGORA.minusDays(1), null);
        prepararAcesso(acesso);

        assertThatThrownBy(() -> useCase.consultar(10L, TOKEN))
                .isInstanceOf(AcompanhamentoPublicoNaoEncontradoException.class);

        verifyNoInteractions(consultarOrcamentoDaOsUseCase, decidirOrcamentoUseCase);
    }

    private void prepararAcessoDisponivel() {
        prepararAcesso(new AcessoAcompanhamento(
                TOKEN_HASH, AGORA.minusDays(1), AGORA.plusDays(1), null));
    }

    private void prepararAcesso(AcessoAcompanhamento acesso) {
        var dados = new AcompanhamentoPublicoGateway.DadosAcompanhamentoPublico(
                NUMERO_OS, StatusOrdemServico.AGUARDANDO_APROVACAO, AGORA.minusDays(1),
                null, null, null, 10L, acesso);
        when(tokenGateway.calcularHash(TOKEN)).thenReturn(TOKEN_HASH);
        when(acompanhamentoGateway.buscarPorTokenHash(TOKEN_HASH)).thenReturn(Optional.of(dados));
    }
}
