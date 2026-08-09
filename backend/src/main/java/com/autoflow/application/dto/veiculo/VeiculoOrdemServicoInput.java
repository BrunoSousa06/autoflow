package com.autoflow.application.dto.veiculo;

public record VeiculoOrdemServicoInput(
        String placa,
        String marca,
        String modelo,
        Integer ano
) {
}
