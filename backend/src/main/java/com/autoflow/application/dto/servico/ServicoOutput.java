package com.autoflow.application.dto.servico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Output DTO for Servico (Service) representing the service at the application layer.
 * Used in all query and command operations at the application layer.
 * Separated from REST DTOs to maintain clean architecture boundaries.
 */
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
