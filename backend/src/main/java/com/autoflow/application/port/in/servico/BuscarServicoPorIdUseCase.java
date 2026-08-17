package com.autoflow.application.port.in.servico;

import com.autoflow.application.dto.servico.ServicoOutput;

public interface BuscarServicoPorIdUseCase {
    ServicoOutput execute(Long id);
}
