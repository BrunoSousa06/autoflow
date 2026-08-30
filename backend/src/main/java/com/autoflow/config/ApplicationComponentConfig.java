package com.autoflow.config;

import com.autoflow.application.gateway.NotificacaoGateway;
import com.autoflow.application.port.in.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCase;
import com.autoflow.application.usecases.ordemservico.acompanhamento.EnviarLinkAcompanhamentoUseCaseImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(
        basePackages = "com.autoflow.application",
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.autoflow\\.application\\.(usecases|security|policy)\\..*(UseCase|Service|Factory|Policy).*"
        ),
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = EnviarLinkAcompanhamentoUseCaseImpl.class
        )
)
public class ApplicationComponentConfig {
    @Bean
    public EnviarLinkAcompanhamentoUseCase enviarLinkAcompanhamentoUseCase(
            NotificacaoGateway notificacaoGateway,
            @Value("${app.frontend-public-base-url}") String frontendPublicBaseUrl) {
        return new EnviarLinkAcompanhamentoUseCaseImpl(notificacaoGateway, frontendPublicBaseUrl);
    }
}
