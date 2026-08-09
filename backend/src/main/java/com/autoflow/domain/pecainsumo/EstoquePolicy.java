package com.autoflow.domain.pecainsumo;

public final class EstoquePolicy {

    private EstoquePolicy() {
    }

    public static EstoqueDisponibilidade classificar(
            int quantidadeDisponivel,
            int quantidadeNecessaria
    ) {
        if (quantidadeDisponivel < 0) {
            throw new IllegalStateException("Quantidade em estoque não pode ser negativa.");
        }
        if (quantidadeNecessaria <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero.");
        }

        return new EstoqueDisponibilidade(
                quantidadeDisponivel >= quantidadeNecessaria,
                quantidadeDisponivel
        );
    }
}
