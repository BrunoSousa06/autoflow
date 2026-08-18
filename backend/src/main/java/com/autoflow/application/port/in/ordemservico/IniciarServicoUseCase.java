package com.autoflow.application.port.in.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public interface IniciarServicoUseCase {
    OrdemServico execute(String numeroOs, Long servicoId);
}
