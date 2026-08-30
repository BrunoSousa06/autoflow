package com.autoflow.application.output.veiculo;

public record VeiculoClienteOutput(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email
) {
}
