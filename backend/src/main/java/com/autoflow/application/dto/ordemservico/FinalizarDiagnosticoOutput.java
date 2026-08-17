package com.autoflow.application.dto.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public record FinalizarDiagnosticoOutput(OrdemServico ordemServico, Long orcamentoId, String publicUrl) {
}
