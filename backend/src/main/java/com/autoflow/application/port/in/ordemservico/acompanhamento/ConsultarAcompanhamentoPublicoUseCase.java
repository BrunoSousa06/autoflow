package com.autoflow.application.port.in.ordemservico.acompanhamento;

import com.autoflow.application.dto.ordemservico.acompanhamento.AcompanhamentoPublicoOutput;

public interface ConsultarAcompanhamentoPublicoUseCase {
    AcompanhamentoPublicoOutput execute(String token);
}
