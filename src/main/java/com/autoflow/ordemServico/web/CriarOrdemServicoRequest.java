package com.autoflow.ordemServico.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CriarOrdemServicoRequest(
        @NotNull UUID clienteId,
        @NotNull UUID veiculoId,
        @NotEmpty List<@Valid ServicoSolicitadoRequest> servicosSolicitados
) {
}