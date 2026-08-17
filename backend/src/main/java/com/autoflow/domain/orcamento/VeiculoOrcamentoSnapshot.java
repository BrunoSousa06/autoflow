package com.autoflow.domain.orcamento;

import com.autoflow.domain.ordemservico.Veiculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoOrcamentoSnapshot {

    private String placa;

    private String marca;

    private String modelo;

    private Integer ano;

    public static VeiculoOrcamentoSnapshot from(Veiculo veiculo) {
        return VeiculoOrcamentoSnapshot.builder()
                .placa(veiculo.placa())
                .marca(veiculo.marca())
                .modelo(veiculo.modelo())
                .ano(veiculo.ano())
                .build();
    }
}
