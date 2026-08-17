package com.autoflow.application.output.ordemservico.reparoadicional;

public record CriarReparoAdicionalOutput(
        Long reparoAdicionalId,
        Long orcamentoId,
        String publicUrl
) {
}
