package com.autoflow.application.port.in.servico;

import com.autoflow.application.input.servico.ServicoInput;
import com.autoflow.application.output.servico.ServicoOutput;

public interface CriarServicoUseCase {
    ServicoOutput execute(ServicoInput input);
}
