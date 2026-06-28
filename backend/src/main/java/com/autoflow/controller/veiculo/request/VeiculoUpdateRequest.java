package com.autoflow.controller.veiculo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VeiculoUpdateRequest(
        @NotBlank(message = "A marca não pode ser vazia") String marca,
        @NotNull(message = "O ano não pode ser vazio") int ano,
        @NotBlank(message = "A placa é obrigatória")
        @Pattern(regexp = "(^[A-Z]{3}\\d{4}$)|(^[A-Z]{3}\\d[A-Z]\\d{2}$)",
                message = "Placa deve estar no formato ABC1234 ou ABC1D23")
        String placa,
        @NotBlank(message = "O modelo nao pode estar vazio")
        String modelo) {
}