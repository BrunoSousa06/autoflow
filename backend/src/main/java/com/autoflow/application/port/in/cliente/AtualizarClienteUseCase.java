package com.autoflow.application.port.in.cliente;

import com.autoflow.application.dto.cliente.ClienteInput;
import com.autoflow.application.dto.cliente.ClienteOutput;

public interface AtualizarClienteUseCase {
    ClienteOutput execute(Long id, ClienteInput input);
}
