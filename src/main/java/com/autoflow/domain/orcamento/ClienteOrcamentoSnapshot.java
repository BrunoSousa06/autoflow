package com.autoflow.domain.orcamento;

import com.autoflow.domain.ordemservico.ClienteOsEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClienteOrcamentoSnapshot {

    @Column(name = "cliente_nome", nullable = false)
    private String nome;

    @Column(name = "cliente_cpf_cnpj", nullable = false)
    private String cpfCnpj;

    @Column(name = "cliente_email", nullable = false)
    private String email;

    @Column(name = "cliente_telefone")
    private String telefone;


    public static ClienteOrcamentoSnapshot from(ClienteOsEntity cliente) {
        return ClienteOrcamentoSnapshot.builder()
                .nome(cliente.getNome())
                .cpfCnpj(cliente.getCpfCnpj())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .build();
    }
}