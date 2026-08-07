package com.autoflow.application.gateway;

import com.autoflow.application.dto.security.CurrentUser;

import java.util.Optional;

public interface CurrentUserGateway {

    Optional<CurrentUser> getCurrentUser();
}
