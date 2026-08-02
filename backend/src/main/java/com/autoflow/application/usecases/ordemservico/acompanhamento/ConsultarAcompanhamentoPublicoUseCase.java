package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoPublicoOutput;
import com.autoflow.application.exception.AcompanhamentoPublicoNaoEncontradoException;
import com.autoflow.application.exception.TokenAcompanhamentoObrigatorioException;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConsultarAcompanhamentoPublicoUseCase {

    private final AcompanhamentoPublicoGateway acompanhamentoGateway;
    private final TokenAcompanhamentoGateway tokenGateway;
    private final Clock clock;

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
