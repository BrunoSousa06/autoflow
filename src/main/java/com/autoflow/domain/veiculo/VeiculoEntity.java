package com.autoflow.domain.veiculo;

import com.autoflow.domain.cliente.ClienteEntity;
import jakarta.persistence.*;
import lombok.*;

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
