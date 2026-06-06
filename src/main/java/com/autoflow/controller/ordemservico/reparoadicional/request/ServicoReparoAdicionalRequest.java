package com.autoflow.controller.ordemservico.reparoadicional.request;

import com.autoflow.controller.ordemservico.request.ItensNecessariosRequest;

import java.util.List;

public record ServicoReparoAdicionalRequest(
        Long servicoId,
        List<ItensNecessariosRequest> itensNecessarios
) {
}
