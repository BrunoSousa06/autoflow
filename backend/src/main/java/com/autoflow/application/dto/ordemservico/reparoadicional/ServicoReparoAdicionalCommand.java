package com.autoflow.application.dto.ordemservico.reparoadicional;

import java.util.List;

public record ServicoReparoAdicionalCommand(
        Long servicoId,
        List<ItemReparoAdicionalCommand> itensNecessarios
) {
}
