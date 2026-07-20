package com.autoflow.application.dto.veiculo;

public record VeiculoFiltro(
        String placa,
        String marca,
        String modelo,
        Integer ano,
        Long clienteId
) {
}
