package com.autoflow.config;

import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.application.usecases.ordemservico.CalcularTempoMedioOrdemServicoUseCase;
import com.autoflow.application.usecases.servico.CalcularTempoMedioServicoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsUseCasesConfig {

    @Bean
    public CalcularTempoMedioOrdemServicoUseCase calcularTempoMedioOrdemServicoUseCase(
            MetricsGateway metricsGateway) {
        return new CalcularTempoMedioOrdemServicoUseCase(metricsGateway);
    }

    @Bean
    public CalcularTempoMedioServicoUseCase calcularTempoMedioServicoUseCase(
            MetricsGateway metricsGateway) {
        return new CalcularTempoMedioServicoUseCase(metricsGateway);
    }
}
