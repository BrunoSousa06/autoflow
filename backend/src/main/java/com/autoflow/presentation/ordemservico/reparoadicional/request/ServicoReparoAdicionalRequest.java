package com.autoflow.presentation.ordemservico.reparoadicional.request;

import com.autoflow.presentation.ordemservico.request.ItensNecessariosRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ServicoReparoAdicionalRequest(
        @NotNull Long servicoId,
        @NotEmpty List<@Valid ItensNecessariosRequest> itensNecessarios
) {
}
