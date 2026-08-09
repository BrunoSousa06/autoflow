package com.autoflow.application.exception;

public class UsuarioNaoAutenticadoException extends RuntimeException {

    public UsuarioNaoAutenticadoException(String message) {
        super(message);
    }
}
