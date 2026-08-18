package com.autoflow.application.port.in.ordemservico;

import com.autoflow.domain.ordemservico.OrdemServico;

public interface AtribuirMecanicoUseCase {
    OrdemServico execute(String numeroOs, Long mecanicoId, String mecanicoEmail);
}
