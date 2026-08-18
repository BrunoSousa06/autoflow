package com.autoflow.application.port.in.servico;

import com.autoflow.application.output.servico.ServicoOutput;

public interface BuscarServicoPorIdUseCase {
    ServicoOutput execute(Long id);
}
