package com.autoflow.presentation.servico.response;


import java.math.BigDecimal;

public record ServicoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal valor,
        boolean ativo) {

}
