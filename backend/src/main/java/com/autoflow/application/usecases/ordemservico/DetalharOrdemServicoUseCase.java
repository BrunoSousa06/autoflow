package com.autoflow.application.usecases.ordemservico;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.application.gateway.OrdemServicoGateway;
import com.autoflow.application.dto.ordemservico.OrdemServicoDetalheOutput;
import com.autoflow.domain.orcamento.StatusOrcamento;
import com.autoflow.domain.ordemservico.OrdemServicoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class DetalharOrdemServicoUseCase {

    private final OrdemServicoGateway ordemServicoGateway;
    private final OrcamentoGateway orcamentoGateway;

    public OrdemServicoDetalheOutput execute(String numeroOs) {
        OrdemServicoEntity ordemServico = ordemServicoGateway.findByNumeroOs(numeroOs)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Ordem de serviço não encontrada."
                ));
        var orcamento = orcamentoGateway.findByNumeroOsAndStatus(numeroOs, StatusOrcamento.DISPONIVEL)
                .or(() -> orcamentoGateway.findTopByNumeroOsOrderByVersaoDesc(numeroOs))
                .orElse(null);
        return new OrdemServicoDetalheOutput(ordemServico, orcamento);
    }
}
