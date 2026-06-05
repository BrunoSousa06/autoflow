package com.autoflow.domain.pecainsumo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaPecaInsumo tipo;

    public void baixarDoEstoque(PecaInsumoEntity pecaInsumo) {
        this.quantidade = this.quantidade - pecaInsumo.getQuantidade();
    }
}
