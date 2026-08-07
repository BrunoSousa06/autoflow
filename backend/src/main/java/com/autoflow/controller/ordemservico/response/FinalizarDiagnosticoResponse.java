package com.autoflow.controller.ordemservico.response;

import com.autoflow.application.dto.ordemservico.FinalizarDiagnosticoOutput;

public record FinalizarDiagnosticoResponse(OrdemServicoResponse ordemServico, Long orcamentoId, String publicUrl) {

    public static FinalizarDiagnosticoResponse from(FinalizarDiagnosticoOutput result) {
        return new FinalizarDiagnosticoResponse(
                OrdemServicoResponse.fromDomain(result.ordemServico()),
                result.orcamentoId(),
                result.publicUrl()
        );
    }
}
