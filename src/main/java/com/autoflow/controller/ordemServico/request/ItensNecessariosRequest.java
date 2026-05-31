package com.autoflow.controller.ordemServico.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItensNecessariosRequest(@NotNull Long pecaInsumoId, @NotNull @Positive Integer quantidade) {
}
