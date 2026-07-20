package com.autoflow.application.dto.veiculo;

public record VeiculoOutput(
        Long id,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        Long clienteId

) {
}
