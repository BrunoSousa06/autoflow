package com.autoflow.application.port.in.usuario;

import com.autoflow.application.input.usuario.RegistroInput;
import com.autoflow.application.output.cliente.ClienteOutput;
import com.autoflow.domain.usuario.Usuario;

public interface CadastrarClienteUseCase {
    ClienteOutput execute(RegistroInput request, Usuario usuario);
}
