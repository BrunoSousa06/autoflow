package com.autoflow.config;

import com.autoflow.application.gateway.MetricsGateway;
import com.autoflow.application.port.in.ordemservico.CalcularTempoMedioOrdemServicoUseCase;
import com.autoflow.application.port.in.servico.CalcularTempoMedioServicoUseCase;
import com.autoflow.application.usecases.ordemservico.CalcularTempoMedioOrdemServicoUseCaseImpl;
import com.autoflow.application.usecases.servico.CalcularTempoMedioServicoUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsUseCasesConfig {

    @Bean
    public CalcularTempoMedioOrdemServicoUseCase calcularTempoMedioOrdemServicoUseCase(
            MetricsGateway metricsGateway) {
        return new CalcularTempoMedioOrdemServicoUseCaseImpl(metricsGateway);
    }

    @Bean
    public CalcularTempoMedioServicoUseCase calcularTempoMedioServicoUseCase(
            MetricsGateway metricsGateway) {
        return new CalcularTempoMedioServicoUseCaseImpl(metricsGateway);
    }
}
