package com.autoflow.application.port.in.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public interface IniciarDiagnosticoUseCase {
    OrdemServico execute(String numeroOs, String emailUsuarioLogado);
}
