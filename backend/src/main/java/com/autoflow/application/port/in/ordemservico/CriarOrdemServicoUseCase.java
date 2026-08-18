package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.output.ordemservico.OrdemServicoCriadaOutput;
import com.autoflow.application.input.ordemservico.CriarOrdemServicoCommand;

public interface CriarOrdemServicoUseCase {
    OrdemServicoCriadaOutput execute(CriarOrdemServicoCommand command);
}
