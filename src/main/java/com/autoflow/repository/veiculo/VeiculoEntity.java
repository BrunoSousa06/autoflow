package com.autoflow.repository.veiculo;

import com.autoflow.repository.cliente.ClienteEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "veiculos")
public class VeiculoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ToString.Exclude            // <--- ISSO CORRIGE O STACKOVERFLOW NO TOSTRING
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private ClienteEntity cliente;
    private String marca;
    private Long ano;
    private String placa;
    private String modelo;
}
