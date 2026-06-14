package com.autoflow.controller.usuario.request;

import com.autoflow.config.validator.CpfCnpj;
import com.autoflow.domain.usuario.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank(message = "O nome é obrigatorio") String nome,
        @NotBlank(message = "O email é obrigatorio") @Email String email,
        @Size(min = 11, max = 14, message = "CPF/CNPJ deve possuir entre 11 e 14 dígitos") @CpfCnpj String cpfCnpj,
        @NotBlank(message = "O telefone é obrigatorio")String telefone,
        @NotBlank(message = "A senha é obrigatoria") @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "A senha deve possuir no mínimo 8 caracteres,uma letra minúscula, uma letra maiúscula, um número e um caractere especial"
        )String senha,
        RoleEnum role
) {

    public RegistroRequest {
        if (role == null) {
            role = RoleEnum.CLIENTE;
        }
    }
}
