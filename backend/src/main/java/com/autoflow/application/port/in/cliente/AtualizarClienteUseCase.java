package com.autoflow.application.port.in.cliente;

import com.autoflow.application.input.cliente.ClienteInput;
import com.autoflow.application.output.cliente.ClienteOutput;

public interface AtualizarClienteUseCase {
    ClienteOutput execute(Long id, ClienteInput input);
}
