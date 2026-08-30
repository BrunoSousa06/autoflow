package com.autoflow.application.input.ordemservico;

import com.autoflow.application.input.veiculo.VeiculoInput;

import java.util.List;

public record CriarOrdemServicoCommand(
        String cpfCnpj,
        VeiculoInput veiculo,
        List<Long> servicoIds) {
}
