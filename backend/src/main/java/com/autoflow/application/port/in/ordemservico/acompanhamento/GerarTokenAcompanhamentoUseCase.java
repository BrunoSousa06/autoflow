package com.autoflow.application.port.in.ordemservico.acompanhamento;

import com.autoflow.application.output.ordemservico.acompanhamento.TokenAcompanhamentoOutput;

public interface GerarTokenAcompanhamentoUseCase {
    TokenAcompanhamentoOutput execute(Long ordemServicoId);
}
