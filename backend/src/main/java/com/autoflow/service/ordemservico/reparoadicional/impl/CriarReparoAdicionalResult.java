package com.autoflow.service.ordemservico.reparoadicional.impl;

public record CriarReparoAdicionalResult(
        Long reparoAdicionalId,
        Long orcamentoId,
        String publicUrl
) {
}