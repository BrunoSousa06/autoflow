package com.autoflow.application.dto.cliente;

import com.autoflow.config.validator.CpfCnpj;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

public record ClienteInput(
        @NotNull(message = "O nome é obrigatório")
        String nome,

        @Size(min = 11, max = 14, message = "CPF/CNPJ deve possuir entre 11 e 14 dígitos")
        @NotNull
        @CpfCnpj
        String cpfCnpj,

        @NotNull(message = "O telefone é obrigatório")
        String telefone,

        @NotNull(message = "O email é obrigatório")
        @Email
        String email
) {}