package com.autoflow.application.input.veiculo;

public record VeiculoOrdemServicoInput(
        String placa,
        String marca,
        String modelo,
        Integer ano
) {
}
