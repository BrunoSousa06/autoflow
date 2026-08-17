package com.autoflow.application.output.servico;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
