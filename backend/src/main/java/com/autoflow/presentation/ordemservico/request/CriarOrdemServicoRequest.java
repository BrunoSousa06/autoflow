package com.autoflow.presentation.ordemservico.request;

import com.autoflow.presentation.validation.CpfCnpj;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CriarOrdemServicoRequest(
        @NotBlank @CpfCnpj String cpfCnpj,
        @NotNull
        @Valid VeiculoOrdemServicoRequest veiculo,
        @NotEmpty List<@Valid ServicoSolicitadoRequest> servicosSolicitados
) {
}
