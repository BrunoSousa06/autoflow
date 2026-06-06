package com.autoflow.controller.ordemservico.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarOrdemServicoRequest(
        @NotNull Long veiculoId,
        @NotEmpty List<@Valid ServicoSolicitadoRequest> servicosSolicitados
) {
}