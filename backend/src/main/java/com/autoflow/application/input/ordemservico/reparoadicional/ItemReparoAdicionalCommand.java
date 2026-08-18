package com.autoflow.application.input.ordemservico.reparoadicional;

public record ItemReparoAdicionalCommand(
        Long pecaInsumoId,
        Integer quantidade
) {
}
