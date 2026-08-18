package com.autoflow.application.port.in.ordemservico.acompanhamento;

import com.autoflow.application.output.ordemservico.acompanhamento.AcompanhamentoPublicoOutput;

public interface ConsultarAcompanhamentoPublicoUseCase {
    AcompanhamentoPublicoOutput execute(String token);
}
