package com.autoflow.service.veiculo.dto;

public record VeiculoFiltro(
        String placa,
        String marca,
        String modelo,
        Integer ano,
        Long clienteId
) {
}
