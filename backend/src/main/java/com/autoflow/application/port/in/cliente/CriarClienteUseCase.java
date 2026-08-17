package com.autoflow.application.port.in.cliente;

import com.autoflow.application.input.cliente.ClienteInput;
import com.autoflow.application.output.cliente.ClienteOutput;

public interface CriarClienteUseCase {
    ClienteOutput execute(ClienteInput input);
}
