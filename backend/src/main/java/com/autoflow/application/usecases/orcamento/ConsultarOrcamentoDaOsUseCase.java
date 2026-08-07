package com.autoflow.application.usecases.orcamento;

import com.autoflow.application.gateway.OrcamentoGateway;
import com.autoflow.domain.orcamento.OrcamentoEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ConsultarOrcamentoDaOsUseCase {
    private final OrcamentoGateway orcamentoGateway;

    public OrcamentoEntity execute(Long id, String numeroOs) {
        OrcamentoEntity orcamento = orcamentoGateway.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Orçamento não encontrado"));
        if (!orcamento.getNumeroOs().equals(numeroOs)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Orçamento não encontrado para esta ordem de serviço");
        }
        return orcamento;
    }
}
