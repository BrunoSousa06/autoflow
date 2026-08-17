package com.autoflow.infrastructure.persistence.entity.orcamento;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class VeiculoOrcamentoSnapshotPersistenceEntity {
    @Column(name = "veiculo_placa", nullable = false)
    private String placa;
    @Column(name = "veiculo_marca")
    private String marca;
    @Column(name = "veiculo_modelo")
    private String modelo;
    @Column(name = "veiculo_ano")
    private Integer ano;
}
