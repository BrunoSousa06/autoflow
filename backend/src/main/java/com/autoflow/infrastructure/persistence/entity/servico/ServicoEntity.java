package com.autoflow.infrastructure.persistence.entity.servico;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "servicos")
public class ServicoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true, nullable = false)
    String nome;
    BigDecimal valor;
    @Column(nullable = false, length = 500)
    String descricao;
    @Column(nullable = false)
    boolean ativo = true;

}
