package com.autoflow.controller.usuario.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "o email é obrigatório") @Email String email,
        @NotBlank(message = "a senha é obrigatória") String senha
) {
}
