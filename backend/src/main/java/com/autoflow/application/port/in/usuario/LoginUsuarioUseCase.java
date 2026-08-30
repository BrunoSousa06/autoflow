package com.autoflow.application.port.in.usuario;

import com.autoflow.application.input.usuario.LoginInput;
import com.autoflow.application.output.usuario.LoginOutput;

public interface LoginUsuarioUseCase {
    LoginOutput execute(LoginInput input);
}
