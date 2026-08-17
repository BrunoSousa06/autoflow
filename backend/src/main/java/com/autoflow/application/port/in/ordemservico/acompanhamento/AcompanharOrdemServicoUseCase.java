package com.autoflow.application.port.in.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoOrdemServicoOutput;

import java.util.List;

public interface AcompanharOrdemServicoUseCase {
    List<AcompanhamentoOrdemServicoOutput> execute(String emailCliente);
}
