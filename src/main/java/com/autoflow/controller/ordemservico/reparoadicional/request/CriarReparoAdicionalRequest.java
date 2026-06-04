package com.autoflow.controller.ordemservico.reparoadicional.request;

import com.autoflow.controller.ordemservico.request.ServicoSolicitadoRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CriarReparoAdicionalRequest(
        @NotEmpty List<@Valid ServicoSolicitadoRequest> servicos
) {
}