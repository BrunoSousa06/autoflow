package com.autoflow.controller.servico.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ServicoRequest(
        @NotBlank(message = "O nome não pode estar em branco")
        String nome,
        BigDecimal valor) {
}
