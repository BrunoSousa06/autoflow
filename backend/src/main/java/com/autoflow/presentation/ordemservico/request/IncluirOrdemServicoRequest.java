package com.autoflow.presentation.ordemservico.request;

import java.util.List;

public record IncluirOrdemServicoRequest(List<ServicoSolicitadoRequest> servicosSolicitados) {
}
