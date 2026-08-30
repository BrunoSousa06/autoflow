package com.autoflow.application.output.cliente;

import lombok.Builder;

import java.util.List;

@Builder
public record ClienteOutput(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        List<ClienteVeiculoOutput> veiculos
) {
}
