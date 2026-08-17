package com.autoflow.application.output.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public record FinalizarDiagnosticoOutput(OrdemServico ordemServico, Long orcamentoId, String publicUrl) {
}
