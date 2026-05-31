package com.autoflow.controller.ordemServico.request;

import jakarta.validation.constraints.NotNull;


public record ServicoSolicitadoRequest(@NotNull Long servicoId) {
}
