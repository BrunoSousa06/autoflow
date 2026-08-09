package com.autoflow.application.dto.ordemservico;

import com.autoflow.domain.ordemservico.StatusOrdemServico;

public record OrdemServicoFiltroInput(String cliente, String numeroOs, StatusOrdemServico status) {
}
