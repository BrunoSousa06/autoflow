package com.autoflow.infrastructure.persistence.entity.orcamento;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoItemNecessarioPersistenceEntity {
    @Column(name = "peca_insumo_id", nullable = false)
    private Long pecaInsumoId;
    @Column(name = "servico_os_id", nullable = false)
    private Long servicoOsId;
    @Column(name = "nome", nullable = false)
    private String nome;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private CategoriaPecaInsumo tipo;
    @Column(name = "valor_unitario", nullable = false)
    private BigDecimal valorUnitario;
    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;
    @Column(name = "valor_total", nullable = false)
    private BigDecimal valorTotal;
}
