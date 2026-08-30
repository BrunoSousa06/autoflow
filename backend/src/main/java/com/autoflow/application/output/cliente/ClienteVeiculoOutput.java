package com.autoflow.application.output.cliente;

public record ClienteVeiculoOutput(
        Long id,
        String marca,
        Long ano,
        String placa,
        String modelo
) {
}
