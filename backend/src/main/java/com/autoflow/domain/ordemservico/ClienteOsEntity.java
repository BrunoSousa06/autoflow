package com.autoflow.domain.ordemservico;

import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClienteOsEntity {

    @Column(name = "cliente_id", nullable = false)
    Long id;
    @Column(name = "cliente_nome", nullable = false)
    String nome;
    @Column(name = "cliente_cpf_cnpj", nullable = false)
    String cpfCnpj;
    @Column(name = "cliente_email", nullable = false)
    String email;
    @Column(name = "cliente_telefone")
    String telefone;

    private ClienteOsEntity(Long id, String nome, String cpfCnpj, String email, String telefone) {
        this.id = id;
        this.nome = nome;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.telefone = telefone;
    }

    public static ClienteOsEntity fromCliente(ClienteEntity c) {
        if (c == null || c.getId() == null) {
            throw new IllegalArgumentException("Cliente invalido para OS.");
        }
        if (c.getNome() == null || c.getNome().isBlank()) {
            throw new IllegalArgumentException("Nome do cliente e obrigatorio.");
        }
        if (c.getCpfCnpj() == null || c.getCpfCnpj().isBlank()) {
            throw new IllegalArgumentException("Cpf/Cnpj do cliente e obrigatorio.");
        }
        if (c.getEmail() == null || c.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email do cliente e obrigatorio.");
        }
        return new ClienteOsEntity(c.getId(), c.getNome(), c.getCpfCnpj(), c.getEmail(), c.getTelefone());
    }
}
