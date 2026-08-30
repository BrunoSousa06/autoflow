package com.autoflow.application.gateway;

import com.autoflow.application.output.security.CurrentUser;

import java.util.Optional;

public interface CurrentUserGateway {

    Optional<CurrentUser> getCurrentUser();
}
