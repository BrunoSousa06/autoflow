package com.autoflow.application.port.in.servico;

import com.autoflow.application.dto.servico.TempoMedioServicoMetricaOutput;

import java.util.List;

public interface CalcularTempoMedioServicoUseCase {
    List<TempoMedioServicoMetricaOutput> execute();
}
