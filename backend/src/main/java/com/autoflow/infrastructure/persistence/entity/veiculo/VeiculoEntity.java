package com.autoflow.infrastructure.persistence.entity.veiculo;

import com.autoflow.infrastructure.persistence.entity.cliente.ClienteEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "veiculos")
public class VeiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private ClienteEntity cliente;
    private String marca;
    private Integer ano;
    @Column(unique = true, nullable = false)
    private String placa;
    private String modelo;
}
