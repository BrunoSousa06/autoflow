package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrcamentoPublicacaoGateway;
import com.autoflow.application.port.in.orcamento.ConsultarOrcamentoPorTokenUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ConsultarOrcamentoPorTokenUseCaseImpl implements ConsultarOrcamentoPorTokenUseCase {
    private final OrcamentoGateway orcamentoGateway;
    private final OrcamentoPublicacaoGateway publicacaoGateway;

    @Override
    public OrcamentoEntity execute(Long id, String token) {
        OrcamentoEntity orcamento = orcamentoGateway.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Orçamento não encontrado"));
        if (!publicacaoGateway.validarToken(orcamento, token)) {
            throw ApplicationException.unauthorized("Token invalido");
        }
        return orcamento;
    }
}
