package com.autoflow.application.port.in.ordemservico;

import com.autoflow.application.output.ordemservico.OrdemServicoDetalheOutput;

public interface DetalharOrdemServicoUseCase {
    OrdemServicoDetalheOutput execute(String numeroOs);
}
