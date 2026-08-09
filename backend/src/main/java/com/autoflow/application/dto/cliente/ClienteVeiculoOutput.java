package com.autoflow.application.dto.cliente;

public record ClienteVeiculoOutput(
        Long id,
        String marca,
        Long ano,
        String placa,
        String modelo
) {
}
