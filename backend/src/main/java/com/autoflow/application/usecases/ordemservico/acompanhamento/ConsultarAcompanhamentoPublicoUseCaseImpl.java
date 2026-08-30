package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.exception.AcompanhamentoPublicoNaoEncontradoException;
import com.autoflow.application.exception.TokenAcompanhamentoObrigatorioException;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.application.output.ordemservico.acompanhamento.AcompanhamentoPublicoOutput;
import com.autoflow.application.port.in.ordemservico.acompanhamento.ConsultarAcompanhamentoPublicoUseCase;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ConsultarAcompanhamentoPublicoUseCaseImpl implements ConsultarAcompanhamentoPublicoUseCase {

    private final AcompanhamentoPublicoGateway acompanhamentoGateway;
    private final TokenAcompanhamentoGateway tokenGateway;
    private final Clock clock;

    @Override
    public AcompanhamentoPublicoOutput execute(String token) {
        validarToken(token);

        String tokenHash = tokenGateway.calcularHash(token);

        var acompanhamento = acompanhamentoGateway
                .buscarPorTokenHash(tokenHash)
                .orElseThrow(
                        AcompanhamentoPublicoNaoEncontradoException::new
                );

        if (!acompanhamento.acesso()
                .estaDisponivelEm(LocalDateTime.now(clock))) {
            throw new AcompanhamentoPublicoNaoEncontradoException();
        }

        return new AcompanhamentoPublicoOutput(
                acompanhamento.numeroOs(),
                acompanhamento.status(),
                acompanhamento.dataAbertura(),
                acompanhamento.execucaoIniciadaEm(),
                acompanhamento.finalizadaEm(),
                acompanhamento.entregueEm(),
                acompanhamento.orcamentoId()
        );
    }

    private void validarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenAcompanhamentoObrigatorioException();
        }
    }
}
