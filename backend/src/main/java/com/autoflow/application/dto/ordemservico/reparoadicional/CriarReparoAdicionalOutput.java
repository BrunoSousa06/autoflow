package com.autoflow.application.dto.ordemservico.reparoadicional;

public record CriarReparoAdicionalOutput(
        Long reparoAdicionalId,
        Long orcamentoId,
        String publicUrl
) {
}
