package com.autoflow.controller.cliente.response;

public record ClienteVeiculoResponse(
        Long id,
        String nome,
        String cpf,
        Long telefone,
        String email
) {
}