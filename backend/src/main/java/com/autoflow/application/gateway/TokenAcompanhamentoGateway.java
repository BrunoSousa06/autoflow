package com.autoflow.application.gateway;

import com.autoflow.application.output.ordemservico.acompanhamento.TokenAcompanhamentoOutput;

public interface TokenAcompanhamentoGateway {

    TokenAcompanhamentoOutput gerar();

    String calcularHash(String token);
}