package com.autoflow.application.dto.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public record OrdemServicoCriadaOutput(OrdemServico ordemServico, String tokenAcompanhamento) {
}
