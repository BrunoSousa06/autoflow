package com.autoflow.controller.ordemServico.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.lang.Long;
import java.util.List;

public record CriarOrdemServicoRequest(
        @NotNull Long veiculoId,
        @NotEmpty List<@Valid ServicoSolicitadoRequest> servicosSolicitados
) {
}