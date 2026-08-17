package com.autoflow.application.output.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public record OrdemServicoCriadaOutput(OrdemServico ordemServico, String tokenAcompanhamento) {
}
