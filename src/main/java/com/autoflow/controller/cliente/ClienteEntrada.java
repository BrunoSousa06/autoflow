package com.autoflow.controller.cliente;


import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.validation.annotation.Validated;

@Getter
@Validated
@AllArgsConstructor
@NoArgsConstructor
public class ClienteEntrada {

    @NonNull
    String nome;
    @NonNull
    String senha;
    @NonNull
    String cpf;
    Long telefone;
    String email;


}
