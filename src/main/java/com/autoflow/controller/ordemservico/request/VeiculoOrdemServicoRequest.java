package com.autoflow.controller.ordemservico.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VeiculoOrdemServicoRequest(
        @NotBlank @Pattern(regexp = "(^[A-Z]{3}[0-9]{4}$)|(^[A-Z]{3}[0-9][A-Z][0-9]{2}$)")
        String placa,
        String marca,
        String modelo,
        Integer ano
) {
}