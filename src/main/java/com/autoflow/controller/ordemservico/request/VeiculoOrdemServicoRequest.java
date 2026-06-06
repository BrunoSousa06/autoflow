package com.autoflow.controller.ordemservico.request;

import jakarta.validation.constraints.NotBlank;

public record VeiculoOrdemServicoRequest(
        @NotBlank String placa,
        String marca,
        String modelo,
        Integer ano
) {
}