package com.autoflow.controller.cliente.response;

public record ClienteVeiculoResponse(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email
) {
}