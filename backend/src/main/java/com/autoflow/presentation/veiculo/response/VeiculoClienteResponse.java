package com.autoflow.presentation.veiculo.response;

public record VeiculoClienteResponse(
        Long id,
        String marca,
        Long ano,
        String placa,
        String modelo) {
}