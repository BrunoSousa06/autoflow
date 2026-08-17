package com.autoflow.application.port.in.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.TokenAcompanhamentoOutput;

public interface GerarTokenAcompanhamentoUseCase {
    TokenAcompanhamentoOutput execute(Long ordemServicoId);
}
