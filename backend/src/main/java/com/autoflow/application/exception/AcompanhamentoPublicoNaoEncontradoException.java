package com.autoflow.application.exception;

public class AcompanhamentoPublicoNaoEncontradoException
        extends RuntimeException {

    public AcompanhamentoPublicoNaoEncontradoException() {
        super("Acompanhamento público não encontrado");
    }
}