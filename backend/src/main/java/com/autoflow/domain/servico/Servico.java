package com.autoflow.domain.servico;

import java.math.BigDecimal;

public record Servico(
        Long id,
        String nome,
        String descricao,
        BigDecimal valor,
        boolean ativo
) {
    public Servico {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome do serviço é obrigatório");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição do serviço é obrigatória");
        }
        if (valor == null || valor.signum() < 0) {
            throw new IllegalArgumentException("O valor do serviço não pode ser negativo");
        }
    }

    public static Servico criar(String nome, String descricao, BigDecimal valor) {
        return new Servico(null, nome, descricao, valor, true);
    }

    public static Servico reconstituir(Long id, String nome, String descricao,
                                       BigDecimal valor, boolean ativo) {
        return new Servico(id, nome, descricao, valor, ativo);
    }

    public Servico atualizar(String nome, String descricao, BigDecimal valor) {
        return new Servico(id, nome, descricao, valor, ativo);
    }
}
