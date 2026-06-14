package com.autoflow.controller.ordemservico.request;

import java.util.List;

public record IncluirOrdemServicoRequest(List<ServicoSolicitadoRequest> servicosSolicitados) {
}
