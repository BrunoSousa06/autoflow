package com.autoflow.application.port.in.cliente;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;

public interface CriarClienteUseCase {
    ClienteOutput execute(ClienteInput input);
}
