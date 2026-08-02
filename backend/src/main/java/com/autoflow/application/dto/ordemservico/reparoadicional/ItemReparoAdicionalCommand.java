package com.autoflow.application.dto.ordemservico.reparoadicional;

public record ItemReparoAdicionalCommand(
        Long pecaInsumoId,
        Integer quantidade
) {
}
