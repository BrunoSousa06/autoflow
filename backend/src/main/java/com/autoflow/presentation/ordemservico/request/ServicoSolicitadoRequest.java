package com.autoflow.presentation.ordemservico.request;

import jakarta.validation.constraints.NotNull;


public record ServicoSolicitadoRequest(@NotNull Long servicoId) {
}
