package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.dto.ordemservico.OrdemServicoDetalheOutput;

public interface DetalharOrdemServicoUseCase {
    OrdemServicoDetalheOutput execute(String numeroOs);
}
