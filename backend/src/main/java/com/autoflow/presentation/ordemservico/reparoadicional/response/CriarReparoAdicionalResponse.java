package com.autoflow.presentation.ordemservico.reparoadicional.response;

public record CriarReparoAdicionalResponse(
        Long reparoAdicionalId,
        Long orcamentoId,
        String publicUrl
) {
}
