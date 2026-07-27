package com.autoflow.application.dto.veiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VeiculoInput(
        @NotBlank(message = "A marca não pode estar em branco")
        String marca,

        @NotNull(message = "O ano não pode ser nulo")
        Integer ano,

        @NotBlank(message = "A placa não pode estar em branco")
        @Pattern(regexp = "^[A-Z]{3}-?[0-9]{4}$|^[A-Z]{3}[0-9]{4}[A-Z]{2}$", 
                 message = "Formato de placa inválido (XXX-1234 ou XXX1234AB)")
        String placa,

        @NotBlank(message = "O modelo não pode estar em branco")
        String modelo
) {
}
