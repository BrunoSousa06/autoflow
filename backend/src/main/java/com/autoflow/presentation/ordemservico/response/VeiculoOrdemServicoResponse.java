package com.autoflow.presentation.ordemservico.response;

import com.autoflow.domain.ordemservico.OrdemServico;

public record VeiculoOrdemServicoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        int ano
) {
    public static VeiculoOrdemServicoResponse fromDomain(OrdemServico os) {
        return new VeiculoOrdemServicoResponse(
                os.getVeiculo().id(),
                os.getVeiculo().placa(),
                os.getVeiculo().marca(),
                os.getVeiculo().modelo(),
                os.getVeiculo().ano()
        );
    }
}
