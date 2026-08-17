package com.autoflow.domain.orcamento;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrcamentoServicoEntity {

    private Long servicoId;

    private String nome;

    private BigDecimal valor;

}
