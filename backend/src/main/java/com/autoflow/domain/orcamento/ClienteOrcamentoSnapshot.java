package com.autoflow.domain.orcamento;

import com.autoflow.domain.ordemservico.ClienteOs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClienteOrcamentoSnapshot {

    private String nome;

    private String cpfCnpj;

    private String email;

    private String telefone;


    public static ClienteOrcamentoSnapshot from(ClienteOs cliente) {
        return ClienteOrcamentoSnapshot.builder()
                .nome(cliente.getNome())
                .cpfCnpj(cliente.getCpfCnpj())
                .email(cliente.getEmail())
                .telefone(cliente.getTelefone())
                .build();
    }
}
