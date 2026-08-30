package com.autoflow.infrastructure.persistence.entity.ordemservico;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ClienteOsEntity {

    @Column(name = "cliente_id", nullable = false)
    private Long id;
    @Column(name = "cliente_nome", nullable = false)
    private String nome;
    @Column(name = "cliente_cpf_cnpj", nullable = false)
    private String cpfCnpj;
    @Column(name = "cliente_email", nullable = false)
    private String email;
    @Column(name = "cliente_telefone")
    private String telefone;
}
