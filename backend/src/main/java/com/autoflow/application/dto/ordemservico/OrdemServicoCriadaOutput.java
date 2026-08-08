package com.autoflow.application.dto.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;

public record OrdemServicoCriadaOutput(OrdemServicoEntity ordemServico, String tokenAcompanhamento) {
}
