package com.autoflow.domain.orcamento;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoItemNecessarioEntity {

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
