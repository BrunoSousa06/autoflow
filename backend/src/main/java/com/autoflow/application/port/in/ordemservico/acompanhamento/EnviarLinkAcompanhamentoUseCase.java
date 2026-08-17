package com.autoflow.application.port.in.ordemservico.acompanhamento;

import com.autoflow.domain.ordemservico.OrdemServico;

public interface EnviarLinkAcompanhamentoUseCase {
    void execute(OrdemServico ordemServico, String token);
}
