package com.autoflow.application.port.in.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;

public interface AtualizarServicoUseCase {
    ServicoOutput execute(Long id, ServicoInput input);
}
