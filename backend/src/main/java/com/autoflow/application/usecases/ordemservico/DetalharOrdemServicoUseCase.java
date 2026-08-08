package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.dto.ordemservico.OrdemServicoDetalheOutput;
import com.autoflow.application.exception.ApplicationException;
import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DetalharOrdemServicoUseCase {

    private final OrdemServicoGateway ordemServicoGateway;
    private final OrcamentoGateway orcamentoGateway;

    public OrdemServicoDetalheOutput execute(String numeroOs) {
        OrdemServicoEntity ordemServico = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> ApplicationException.notFound(
                        "Ordem de serviço não encontrada."
                ));
        var orcamento = orcamentoGateway.findByNumeroOsAndStatus(numeroOs, StatusOrcamento.DISPONIVEL)
                .or(() -> orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc(numeroOs))
                .orElse(null);
        return new OrdemServicoDetalheOutput(ordemServico, orcamento);
    }
}
