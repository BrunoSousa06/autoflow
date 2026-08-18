package com.autoflow.infrastructure.configuration;

import com.autoflow.application.gateway.AcompanhamentoUrlGateway;
import org.springframework.stereotype.Component;

@Component
public class AcompanhamentoUrlAdapter implements AcompanhamentoUrlGateway {

    private final FrontendPublicProperties properties;

    public AcompanhamentoUrlAdapter(FrontendPublicProperties properties) {
        this.properties = properties;
    }

    @Override
    public String gerar(String token) {
        return properties.getFrontendPublicBaseUrl() + "/public/acompanhamento?token=" + token;
    }
}
