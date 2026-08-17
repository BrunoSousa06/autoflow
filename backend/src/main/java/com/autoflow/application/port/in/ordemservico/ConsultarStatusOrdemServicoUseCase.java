package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.output.ordemservico.StatusOrdemServicoOutput;

public interface ConsultarStatusOrdemServicoUseCase {
    StatusOrdemServicoOutput execute(String numeroOs, String emailUsuarioAutenticado);
}
