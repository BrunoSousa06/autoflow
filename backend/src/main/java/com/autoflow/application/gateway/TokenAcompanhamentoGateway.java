package com.autoflow.application.gateway;

import com.autoflow.application.dto.ordemservico.acompanhamento.TokenAcompanhamentoOutput;

public interface TokenAcompanhamentoGateway {

    TokenAcompanhamentoOutput gerar();

    String calcularHash(String token);
}