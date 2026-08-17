package com.autoflow.application.port.in.usuario;

import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.application.dto.usuario.UsuarioOutput;

public interface CadastrarStaffUseCase {
    UsuarioOutput execute(RegistroInput request);
}
