package com.autoflow.application.port.in.servico;

import com.autoflow.application.dto.servico.ServicoInput;
import com.autoflow.application.dto.servico.ServicoOutput;

public interface CriarServicoUseCase {
    ServicoOutput execute(ServicoInput input);
}
