package com.autoflow.application.port.in.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public interface FinalizarServicoUseCase {
    OrdemServico execute(String numeroOs, Long servicoId);
}
