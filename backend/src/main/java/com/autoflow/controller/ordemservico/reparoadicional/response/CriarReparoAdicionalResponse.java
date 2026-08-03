package com.autoflow.controller.ordemservico.reparoadicional.response;

public record CriarReparoAdicionalResponse(
        Long reparoAdicionalId,
        Long orcamentoId,
        String publicUrl
) {
}
