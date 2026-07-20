package com.autoflow.application.dto.servico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicoOutput {

    private Long id;

    private String nome;

    private String descricao;

    private BigDecimal valor;

    private boolean ativo;

}
