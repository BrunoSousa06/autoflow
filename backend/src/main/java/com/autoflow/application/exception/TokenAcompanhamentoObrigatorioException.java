package com.autoflow.application.exception;

public class TokenAcompanhamentoObrigatorioException
        extends RuntimeException {

    public TokenAcompanhamentoObrigatorioException() {
        super("Token de acompanhamento é obrigatório");
    }
}