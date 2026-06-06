package com.autoflow.controller.ordemservico.reparoadicional.response;

import com.autoflow.service.ordemservico.reparoadicional.impl.CriarReparoAdicionalResult;

public record CriarReparoAdicionalResponse(
        Long reparoAdicionalId,
        Long orcamentoId,
        String publicUrl
) {
    public static CriarReparoAdicionalResponse from(CriarReparoAdicionalResult result) {
        return new CriarReparoAdicionalResponse(
                result.reparoAdicionalId(),
                result.orcamentoId(),
                result.publicUrl()
        );
    }
}