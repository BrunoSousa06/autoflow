package com.autoflow.ordemServico.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ServicoSolicitadoRequest(@NotNull UUID servicoId, @NotBlank String nome) {
}
