package com.autoflow.controller.ordemservico.response;

import com.autoflow.service.ordemservico.dto.FinalizarDiagnosticoResult;

public record FinalizarDiagnosticoResponse(OrdemServicoResponse ordemServico, Long orcamentoId, String publicUrl) {

    public static FinalizarDiagnosticoResponse from(FinalizarDiagnosticoResult result) {
        return new FinalizarDiagnosticoResponse(
                OrdemServicoResponse.fromDomain(result.ordemServico()),
                result.orcamentoId(),
                result.publicUrl()
        );
    }
}
