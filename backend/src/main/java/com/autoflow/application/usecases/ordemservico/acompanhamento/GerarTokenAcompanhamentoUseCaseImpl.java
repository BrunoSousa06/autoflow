package com.autoflow.application.usecases.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.TokenAcompanhamentoOutput;
import com.autoflow.application.gateway.AcompanhamentoPublicoGateway;
import com.autoflow.application.gateway.TokenAcompanhamentoGateway;
import com.autoflow.application.port.in.ordemservico.acompanhamento.GerarTokenAcompanhamentoUseCase;
import com.autoflow.domain.ordemservico.acompanhamento.AcessoAcompanhamento;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;


@RequiredArgsConstructor
public class GerarTokenAcompanhamentoUseCaseImpl implements GerarTokenAcompanhamentoUseCase {

    private static final long VALIDADE_TOKEN_EM_DIAS = 30;

    private final TokenAcompanhamentoGateway tokenGateway;
    private final AcompanhamentoPublicoGateway acompanhamentoGateway;
    private final Clock clock;

    @Override
    public TokenAcompanhamentoOutput execute(
            Long ordemServicoId
    ) {
        validarOrdemServicoId(ordemServicoId);

        TokenAcompanhamentoOutput tokenGerado =
                tokenGateway.gerar();

        LocalDateTime criadoEm =
                LocalDateTime.now(clock);

        AcessoAcompanhamento acesso =
                new AcessoAcompanhamento(
                        tokenGerado.hash(),
                        criadoEm,
                        criadoEm.plusDays(
                                VALIDADE_TOKEN_EM_DIAS
                        ),
                        null
                );

        acompanhamentoGateway.salvar(
                ordemServicoId,
                acesso
        );

        return tokenGerado;
    }

    private void validarOrdemServicoId(
            Long ordemServicoId
    ) {
        if (ordemServicoId == null) {
            throw new IllegalArgumentException(
                    "ID da ordem de serviço é obrigatório"
            );
        }

        if (ordemServicoId <= 0) {
            throw new IllegalArgumentException(
                    "ID da ordem de serviço deve ser positivo"
            );
        }
    }
}
