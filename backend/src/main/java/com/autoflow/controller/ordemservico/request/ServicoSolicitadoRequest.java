package com.autoflow.controller.ordemservico.request;

import jakarta.validation.constraints.NotNull;


public record ServicoSolicitadoRequest(@NotNull Long servicoId) {
}
