package com.autoflow.application.port.in.servico;

import com.autoflow.application.input.servico.ServicoInput;
import com.autoflow.application.output.servico.ServicoOutput;

public interface AtualizarServicoUseCase {
    ServicoOutput execute(Long id, ServicoInput input);
}
