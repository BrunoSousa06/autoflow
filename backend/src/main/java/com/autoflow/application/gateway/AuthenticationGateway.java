package com.autoflow.application.gateway;

public interface AuthenticationGateway {

    void authenticate(String email, String senha);
}
