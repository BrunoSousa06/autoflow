package com.autoflow.application.port.in.usuario;

import com.autoflow.application.dto.usuario.LoginInput;
import com.autoflow.application.dto.usuario.LoginOutput;

public interface LoginUsuarioUseCase {
    LoginOutput execute(LoginInput input);
}
