package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.exception.AcompanhamentoPublicoNaoEncontradoException;
import com.autoflow.application.exception.TokenAcompanhamentoObrigatorioException;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.application.port.in.ordemservico.acompanhamento.AcessarOrcamentoAcompanhamentoUseCase;
import com.autoflow.application.port.in.orcamento.ConsultarOrcamentoDaOsUseCase;
import com.autoflow.application.port.in.orcamento.DecidirOrcamentoUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;


@RequiredArgsConstructor
public class AcessarOrcamentoAcompanhamentoUseCaseImpl implements AcessarOrcamentoAcompanhamentoUseCase {

    private final AcompanhamentoPublicoGateway acompanhamentoGateway;
    private final TokenAcompanhamentoGateway tokenGateway;
    private final ConsultarOrcamentoDaOsUseCase consultarOrcamentoDaOsUseCase;
    private final DecidirOrcamentoUseCase decidirOrcamentoUseCase;
    private final Clock clock;

    @Override
    public OrcamentoEntity consultar(Long orcamentoId, String token) {
        return consultarOrcamentoDaOsUseCase.execute(orcamentoId, validarEObterNumeroOs(token));
    }

    @Override
    public OrcamentoEntity aprovar(Long orcamentoId, String token) {
        return decidirOrcamentoUseCase.aprovarDaOrdem(orcamentoId, validarEObterNumeroOs(token));
    }

    private String validarEObterNumeroOs(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenAcompanhamentoObrigatorioException();
        }

        var acompanhamento = acompanhamentoGateway
                .buscarPorTokenHash(tokenGateway.calcularHash(token))
                .orElseThrow(AcompanhamentoPublicoNaoEncontradoException::new);

        if (!acompanhamento.acesso().estaDisponivelEm(LocalDateTime.now(clock))) {
            throw new AcompanhamentoPublicoNaoEncontradoException();
        }

        return acompanhamento.numeroOs();
    }
}
