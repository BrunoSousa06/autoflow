package com.autoflow.controller.cliente.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;


@Validated
public record ClienteRequest(
        @NotNull(message = "O nome é obrigatório") String nome,
        @Size(min = 11, max = 14, message = "CPF/CNPJ deve possuir entre 11 e 14 dígitos")
        @NotNull String cpfCnpj,
        @NotNull(message = "O telefone é obrigatório") String telefone,
        @NotNull(message = "O email é obrigatório") String email) {

}
