package com.autoflow.application.input.cliente;

public record ClienteInput(
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        Long usuarioId
) {

    public ClienteInput(String nome, String cpfCnpj, String telefone, String email) {
        this(nome, cpfCnpj, telefone, email, null);
    }
}
