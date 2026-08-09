package com.autoflow.application.gateway;

public interface TokenGateway {

    String generateToken(String email, String role);
}
