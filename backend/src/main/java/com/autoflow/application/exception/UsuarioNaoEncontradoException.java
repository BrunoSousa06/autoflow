package com.autoflow.application.exception;

public class UsuarioNaoEncontradoException extends RuntimeException {
    public UsuarioNaoEncontradoException() {
        super("Usuário autenticado não encontrado.");
    }
}
