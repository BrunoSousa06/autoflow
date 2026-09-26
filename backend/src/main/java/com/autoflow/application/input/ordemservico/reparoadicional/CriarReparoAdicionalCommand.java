package com.autoflow.application.input.ordemservico.reparoadicional;

import java.util.List;

public record CriarReparoAdicionalCommand(
        String numeroOs,
        String cpfCnpjMecanico,
        List<ServicoReparoAdicionalCommand> servicos
) {
}
