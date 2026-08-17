package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.port.in.orcamento.ConsultarOrcamentoDaOsUseCase;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ConsultarOrcamentoDaOsUseCaseImpl implements ConsultarOrcamentoDaOsUseCase {
    private final OrcamentoGateway orcamentoGateway;

    @Override
    public OrcamentoEntity execute(Long id, String numeroOs) {
        OrcamentoEntity orcamento = orcamentoGateway.findById(id)
                .orElseThrow(() -> ApplicationException.notFound("Orçamento não encontrado"));
        if (!orcamento.getNumeroOs().equals(numeroOs)) {
            throw ApplicationException.notFound(
                    "Orçamento não encontrado para esta ordem de serviço");
        }
        return orcamento;
    }
}
