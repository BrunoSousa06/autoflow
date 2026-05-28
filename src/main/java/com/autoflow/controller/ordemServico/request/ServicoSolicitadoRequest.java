package com.autoflow.controller.ordemServico.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


//TODO QUANDO TIVER ENTIDADE DE SERVICOS RECEBER SO O SERVICOID
public record ServicoSolicitadoRequest(@NotNull Long servicoId, @NotBlank String descricao) {
}
