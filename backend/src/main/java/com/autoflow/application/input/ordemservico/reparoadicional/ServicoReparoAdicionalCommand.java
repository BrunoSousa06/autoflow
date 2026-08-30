package com.autoflow.application.input.ordemservico.reparoadicional;

import java.util.List;

public record ServicoReparoAdicionalCommand(
        Long servicoId,
        List<ItemReparoAdicionalCommand> itensNecessarios
) {
}
