package com.autoflow.application.port.in.cliente;

import com.autoflow.application.dto.cliente.ClienteOutput;

public interface ListarClienteUseCase {
    ClienteOutput execute(Long documento);
}
