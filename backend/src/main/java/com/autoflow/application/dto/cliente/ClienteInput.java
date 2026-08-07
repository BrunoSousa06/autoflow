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
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        Long usuarioId
) {

    public ClienteInput(String nome, String cpfCnpj, String telefone, String email) {
        this(nome, cpfCnpj, telefone, email, null);
    }
}
