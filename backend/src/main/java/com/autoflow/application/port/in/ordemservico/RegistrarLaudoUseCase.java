package com.autoflow.application.port.in.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public interface RegistrarLaudoUseCase {
    OrdemServico execute(String numeroOs, String emailUsuarioLogado, String laudo);
}
