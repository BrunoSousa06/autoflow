package com.autoflow.domain.ordemservico;

import com.autoflow.domain.cliente.Cliente;

public class ClienteOs {

    private Long id;
    private String nome;
    private String cpfCnpj;
    private String email;
    private String telefone;

    private ClienteOs(Long id, String nome, String cpfCnpj, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.telefone = telefone;
    }

    public static ClienteOs fromCliente(Cliente cliente) {
        if (cliente == null || cliente.id() == null) {
            throw new IllegalArgumentException("Cliente invalido para OS.");
        }
        return fromFields(cliente.id(), cliente.nome(), cliente.cpfCnpj(), cliente.email(), cliente.telefone());
    }

    public static ClienteOs fromFields(
            Long id,
            String nome,
            String cpfCnpj,
            String email,
            String telefone) {
        if (id == null || nome == null || nome.isBlank()
                || cpfCnpj == null || cpfCnpj.isBlank()
                || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Dados do cliente invalidos para OS.");
        }
        return new ClienteOs(id, nome, cpfCnpj, email, telefone);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }
}
