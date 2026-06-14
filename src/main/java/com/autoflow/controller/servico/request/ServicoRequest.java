package com.autoflow.controller.servico.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ServicoRequest(
        @NotBlank(message = "O nome não pode estar em branco")
        String nome,
        @NotBlank(message = "A descrição não pode estar em branco")
        String descricao,
        BigDecimal valor) {
}
