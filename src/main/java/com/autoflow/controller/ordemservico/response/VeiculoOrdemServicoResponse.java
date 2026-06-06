package com.autoflow.controller.ordemservico.response;

import com.autoflow.domain.ordemservico.OrdemServicoEntity;

public record VeiculoOrdemServicoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        int ano
) {
    public static VeiculoOrdemServicoResponse fromDomain(OrdemServicoEntity os) {
        return new VeiculoOrdemServicoResponse(
                os.getVeiculo().getId(),
                os.getVeiculo().getPlaca(),
                os.getVeiculo().getMarca(),
                os.getVeiculo().getModelo(),
                os.getVeiculo().getAno()
        );
    }
}
