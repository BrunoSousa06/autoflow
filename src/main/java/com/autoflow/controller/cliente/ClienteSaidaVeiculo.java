package com.autoflow.controller.cliente;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClienteSaidaVeiculo {


    Long id;
    String nome;
    String cpf;
    Long telefone;
    String email;

}