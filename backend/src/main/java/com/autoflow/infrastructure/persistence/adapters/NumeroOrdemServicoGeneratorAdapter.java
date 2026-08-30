package com.autoflow.infrastructure.persistence.adapters;

import com.autoflow.application.gateway.NumeroOrdemServicoGateway;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NumeroOrdemServicoGeneratorAdapter implements NumeroOrdemServicoGateway {

    @Override
    public String gerar() {
        return "OS-" + UUID.randomUUID();
    }
}
