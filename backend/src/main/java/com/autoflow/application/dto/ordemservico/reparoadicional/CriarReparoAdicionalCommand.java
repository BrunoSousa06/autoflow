package com.autoflow.application.dto.ordemservico.reparoadicional;

import java.util.List;

public record CriarReparoAdicionalCommand(
        String numeroOs,
        String emailMecanico,
        List<ServicoReparoAdicionalCommand> servicos
) {
}
