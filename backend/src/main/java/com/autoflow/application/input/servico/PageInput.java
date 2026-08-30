package com.autoflow.application.input.servico;

public record PageInput(
        int page,
        int size
) {
    public PageInput {
        if (page < 0) {
            throw new IllegalArgumentException("A página não pode ser negativa");
        }
        if (size < 1) {
            throw new IllegalArgumentException("O tamanho da página deve ser maior que zero");
        }
    }
}
