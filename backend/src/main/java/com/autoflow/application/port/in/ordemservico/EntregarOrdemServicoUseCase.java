package com.autoflow.application.port.in.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public interface EntregarOrdemServicoUseCase {
    OrdemServico execute(String numeroOs);
}
