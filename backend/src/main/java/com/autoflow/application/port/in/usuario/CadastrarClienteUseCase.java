package com.autoflow.application.port.in.usuario;

import com.autoflow.application.dto.cliente.ClienteOutput;
import com.autoflow.application.dto.usuario.RegistroInput;
import com.autoflow.domain.usuario.Usuario;

public interface CadastrarClienteUseCase {
    ClienteOutput execute(RegistroInput request, Usuario usuario);
}
