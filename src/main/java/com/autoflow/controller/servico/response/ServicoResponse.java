package com.autoflow.controller.servico.response;


import java.math.BigDecimal;

public record ServicoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal valor) {

}
