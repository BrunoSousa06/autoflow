package com.autoflow.application.input;

public record PageQuery(int page, int size) {
    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("Página deve ser maior ou igual a zero.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Tamanho da página deve ser maior que zero.");
        }
    }
}
