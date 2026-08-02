package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.exception.AcompanhamentoPublicoNaoEncontradoException;
import com.autoflow.application.exception.TokenAcompanhamentoObrigatorioException;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import com.autoflow.service.orcamento.OrcamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AcessarOrcamentoAcompanhamentoUseCase {

    private final AcompanhamentoPublicoGateway acompanhamentoGateway;
    private final TokenAcompanhamentoGateway tokenGateway;
    private final OrcamentoService orcamentoService;
    private final Clock clock;

    public OrcamentoEntity consultar(Long orcamentoId, String token) {
        return orcamentoService.consultarDaOrdem(orcamentoId, validarEObterNumeroOs(token));
    }

    public OrcamentoEntity aprovar(Long orcamentoId, String token) {
        return orcamentoService.aprovarDaOrdem(orcamentoId, validarEObterNumeroOs(token));
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
