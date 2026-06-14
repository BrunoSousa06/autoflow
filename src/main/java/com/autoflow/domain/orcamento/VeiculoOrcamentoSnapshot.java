package com.autoflow.domain.orcamento;

import com.autoflow.domain.veiculo.VeiculoEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoOrcamentoSnapshot {

    @Column(name = "veiculo_placa", nullable = false)
    private String placa;

    @Column(name = "veiculo_marca")
    private String marca;

    @Column(name = "veiculo_modelo")
    private String modelo;

    @Column(name = "veiculo_ano")
    private Integer ano;

    public static VeiculoOrcamentoSnapshot from(VeiculoEntity veiculo) {
        return VeiculoOrcamentoSnapshot.builder()
                .placa(veiculo.getPlaca())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .ano(veiculo.getAno())
                .build();
    }
}
