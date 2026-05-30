package com.autoflow.domain.pecaInsumo;

import com.autoflow.domain.veiculo.VeiculoEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "pecas_insumos")
public class PecaInsumoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String nome;
    private int quantidade;
    private BigDecimal valor;

}
