package com.autoflow.domain.orcamento;

import com.autoflow.domain.ordemservico.ClienteOs;

import java.util.Objects;

public class ClienteOrcamentoSnapshot {

    private String nome;

    private String cpfCnpj;

    private String email;

    private String telefone;

    public ClienteOrcamentoSnapshot() {
    }

    public ClienteOrcamentoSnapshot(String nome, String cpfCnpj, String email, String telefone) {
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.telefone = telefone;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ClienteOrcamentoSnapshot from(ClienteOs cliente) {
        return ClienteOrcamentoSnapshot.builder()
                .nome(cliente.getNome())
                .cpfCnpj(cliente.getCpfCnpj())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .build();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof ClienteOrcamentoSnapshot that)) return false;
        return Objects.equals(nome, that.nome)
                && Objects.equals(cpfCnpj, that.cpfCnpj)
                && Objects.equals(email, that.email)
                && Objects.equals(telefone, that.telefone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, cpfCnpj, email, telefone);
    }

    @Override
    public String toString() {
        return "ClienteOrcamentoSnapshot{" +
                "nome='" + nome + '\'' +
                ", cpfCnpj='" + cpfCnpj + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                '}';
    }

    public static final class Builder {
        private String nome;
        private String cpfCnpj;
        private String email;
        private String telefone;

        public Builder nome(String nome) {
            this.nome = nome;
            return this;
        }

        public Builder cpfCnpj(String cpfCnpj) {
            this.cpfCnpj = cpfCnpj;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder telefone(String telefone) {
            this.telefone = telefone;
            return this;
        }

        public ClienteOrcamentoSnapshot build() {
            return new ClienteOrcamentoSnapshot(nome, cpfCnpj, email, telefone);
        }
    }
}
