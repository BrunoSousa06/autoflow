package com.autoflow.application.input.veiculo;

public record VeiculoFiltro(
        String placa,
        String marca,
        String modelo,
        Integer ano,
        Long clienteId
) {
}
