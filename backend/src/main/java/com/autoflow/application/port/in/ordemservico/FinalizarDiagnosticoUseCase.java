package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.dto.ordemservico.FinalizarDiagnosticoOutput;

public interface FinalizarDiagnosticoUseCase {
    FinalizarDiagnosticoOutput execute(String numeroOs, String emailUsuarioLogado);
}
