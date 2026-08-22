package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.input.ordemservico.CriarOrdemServicoCommand;
import com.autoflow.application.output.ordemservico.OrdemServicoCriadaOutput;

public interface CriarOrdemServicoUseCase {
    OrdemServicoCriadaOutput execute(CriarOrdemServicoCommand command);
}
