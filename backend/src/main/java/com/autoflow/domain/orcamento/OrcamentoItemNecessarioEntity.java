package com.autoflow.domain.orcamento;

import com.autoflow.domain.pecainsumo.CategoriaPecaInsumo;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrcamentoItemNecessarioEntity {

    private Long pecaInsumoId;

    private Long servicoOsId;

    private String nome;

    private CategoriaPecaInsumo tipo;

    private BigDecimal valorUnitario;

    private Integer quantidade;

    private BigDecimal valorTotal;
}
