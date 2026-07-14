package com.autoflow.presentation.cliente.response;

public record ClienteVeiculoResponse(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email
) {
}