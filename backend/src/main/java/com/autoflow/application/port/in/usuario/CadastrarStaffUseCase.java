package com.autoflow.application.port.in.usuario;

import com.autoflow.application.input.usuario.RegistroInput;
import com.autoflow.application.output.usuario.UsuarioOutput;

public interface CadastrarStaffUseCase {
    UsuarioOutput execute(RegistroInput request);
}
