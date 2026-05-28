package com.autoflow.controller.ordemServico.request;

import java.util.List;

public record IncluirOrdemServicoRequest(List<ServicoSolicitadoRequest> servicosSolicitados) {
}
