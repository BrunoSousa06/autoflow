package com.autoflow.application.dto.servico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Output DTO for Service Metrics at the application layer.
 * Contains aggregated execution time data for a service.
 * Used by CalcularTempoMedioServicoUseCase.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempoMedioServicoMetricaOutput {

    private Long servicoId;

    private String nomeServico;

    private Long quantidadeExecucoes;

    private Double tempoMedioSegundos;

    private Double tempoMedioMinutos;

    private Double tempoMedioHoras;

}
