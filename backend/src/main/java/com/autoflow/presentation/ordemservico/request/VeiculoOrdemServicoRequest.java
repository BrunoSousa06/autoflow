package com.autoflow.presentation.ordemservico.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VeiculoOrdemServicoRequest(
        @NotBlank @Pattern(regexp = "(^[A-Z]{3}\\d{4}$)|(^[A-Z]{3}\\d[A-Z]\\d{2}$)")
        String placa,
        String marca,
        String modelo,
        Integer ano
) {
}
