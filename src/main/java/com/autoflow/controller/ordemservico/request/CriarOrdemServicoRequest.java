package com.autoflow.controller.ordemservico.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarOrdemServicoRequest(
        @NotBlank String cpfCnpj,
        @NotNull Long veiculoId,
        @NotEmpty List<@Valid ServicoSolicitadoRequest> servicosSolicitados
) {
}