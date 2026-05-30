package com.autoflow.controller.veiculo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record VeiculoRequest(
        @NotNull Long idCliente,
        @NotBlank(message = "A marca não pode ser vazia") String marca,
        @NotBlank(message = "O ano não pode ser vazio") int ano,
        @NotBlank(message = "A placa é obrigatória")
        @Pattern(regexp = "(^[A-Z]{3}[0-9]{4}$)|(^[A-Z]{3}[0-9][A-Z][0-9]{2}$)",
                message = "Placa deve estar no formato ABC1234 ou ABC1D23")
        String placa,
        @NotBlank(message = "O modelo nao pode estar vazio")
        String modelo) {
}

