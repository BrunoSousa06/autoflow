package com.autoflow.application.dto.veiculo;

public record VeiculoClienteOutput(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email
) {
}
