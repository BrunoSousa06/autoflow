package com.autoflow.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class FrontendPublicProperties {

    private String frontendPublicBaseUrl;

    public String getFrontendPublicBaseUrl() {
        return frontendPublicBaseUrl;
    }

    public void setFrontendPublicBaseUrl(String frontendPublicBaseUrl) {
        this.frontendPublicBaseUrl = frontendPublicBaseUrl;
    }
}
